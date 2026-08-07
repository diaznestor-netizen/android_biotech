package com.biobox.biotech.data.repository

import android.content.Context
import android.util.Log
import androidx.work.WorkManager
import com.biobox.biotech.core.datastore.SessionDataStore
import com.biobox.biotech.core.util.AppConstants
import com.biobox.biotech.core.util.SecurityUtils
import com.biobox.biotech.data.local.dao.SyncOperationDao
import com.biobox.biotech.data.local.database.BioTechDatabase
import com.biobox.biotech.data.mapper.toDomain
import com.biobox.biotech.data.remote.api.AuthService
import com.biobox.biotech.data.remote.dto.LoginRequest
import com.biobox.biotech.core.util.safeApiCall
import com.biobox.biotech.data.remote.dto.OtpActionRequest
import com.biobox.biotech.data.remote.dto.PasswordlessLoginRequest
import com.biobox.biotech.data.remote.dto.PasswordRecoveryConfirmRequest
import com.biobox.biotech.data.remote.dto.PasswordRecoveryRequest
import com.biobox.biotech.domain.model.User
import com.biobox.biotech.domain.repository.AuthRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

class TwoFactorRequiredException(
    val sessionId: String,
    override val message: String
) : Exception(message)

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authService: AuthService,
    @Named("AuthenticatedAuthService") private val authenticatedAuthService: AuthService,
    private val sessionDataStore: SessionDataStore,
    private val database: BioTechDatabase,
    private val syncOperationDao: SyncOperationDao,
    private val workManager: WorkManager,
    private val notificationCenter: com.biobox.biotech.domain.notifications.NotificationCenter,
    @ApplicationContext private val context: Context
) : AuthRepository {

    override val currentUser: Flow<User?> = sessionDataStore.userData.map { it?.toDomain() }

    override fun getPendingSyncCount(): Flow<Int> = syncOperationDao.getPendingCount()

    override suspend fun login(phoneNumber: String, password: String): Result<User> {
        val phoneMasked = SecurityUtils.ofuscarTelefono(phoneNumber)
        Log.d(AppConstants.TAG_AUTH, "Iniciando autenticación para: $phoneMasked")

        return safeApiCall(
            apiCall = { authService.login(LoginRequest(phoneNumber, password)) },
            transform = { it }
        ).mapCatching { body ->
            if (body.requires2FA) {
                throw TwoFactorRequiredException(
                    sessionId = body.sessionId.orEmpty(),
                    message = body.message ?: "Se requiere verificacion por Telegram"
                )
            }
            val accessToken = body.tokens?.accessToken
                ?: throw Exception("El servidor no devolvio access token")
            val user = body.user ?: throw Exception("El servidor no devolvio usuario")
            sessionDataStore.saveSession(accessToken = accessToken, user = user)
            user.toDomain()
        }.onFailure { error ->
            Log.e(AppConstants.TAG_AUTH, "Error en login ($phoneMasked): ${error.message}")
        }
    }

    override suspend fun loginWithDailyCode(telefono: String, codigo: String): Result<User> {
        val phoneMasked = SecurityUtils.ofuscarTelefono(telefono)
        Log.d(AppConstants.TAG_AUTH, "Iniciando autenticación passwordless para: $phoneMasked")

        return safeApiCall(
            apiCall = { authService.loginPasswordless(PasswordlessLoginRequest(telefono, codigo)) },
            transform = { it }
        ).mapCatching { body ->
            val accessToken = body.tokens?.accessToken
                ?: throw Exception("El servidor no devolvio access token")
            val user = body.user ?: throw Exception("El servidor no devolvio usuario")
            sessionDataStore.saveSession(accessToken = accessToken, user = user)
            user.toDomain()
        }.onFailure { error ->
            Log.e(AppConstants.TAG_AUTH, "Error en login passwordless ($phoneMasked): ${error.message}")
        }
    }

    override suspend fun verifySecondFactor(sessionId: String, code: String): Result<User> {
        return try {
            Log.d("2FA_DEBUG", "verify-2fa session_id=$sessionId code=$code")
            val response = authService.verifySecondFactor(mapOf("session_id" to sessionId, "code" to code))
            if (!response.isSuccessful) {
                return Result.failure(Exception(parseAuthError(response.errorBody()?.string(), "Codigo invalido")))
            }
            val body = response.body() ?: return Result.failure(Exception("Respuesta vacia del servidor"))
            val accessToken = body.tokens?.accessToken
                ?: return Result.failure(Exception("El servidor no devolvio access token"))
            val user = body.user ?: return Result.failure(Exception("El servidor no devolvio usuario"))
            sessionDataStore.saveSession(accessToken = accessToken, user = user)
            Result.success(user.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        try {
            authenticatedAuthService.logout()
        } catch (_: Exception) {
        }

        workManager.cancelAllWork()
        clearRoomData()
        sessionDataStore.clearSession()
        clearLocalFiles()
    }

    private suspend fun clearRoomData() {
        withContext(Dispatchers.IO) {
            database.clearAllTables()
        }
    }

    private fun clearLocalFiles() {
        context.cacheDir.listFiles()?.forEach { file ->
            if (file.isDirectory) file.deleteRecursively() else file.delete()
        }
        listOf("temp", "uploads", "downloads")
            .map { File(context.filesDir, it) }
            .filter { it.exists() }
            .forEach { it.deleteRecursively() }
    }

    override suspend fun checkSession(): Boolean {
        return sessionDataStore.authToken.first()?.isNotBlank() == true
    }

    override suspend fun refreshSession(): Result<Unit> {
        return try {
            val response = authService.refresh()
            if (!response.isSuccessful) {
                sessionDataStore.clearSession()
                return Result.failure(Exception("Tu sesion expiro. Inicia sesion nuevamente."))
            }
            val body = response.body() ?: return Result.failure(Exception("Respuesta vacia del servidor"))
            val accessToken = body.tokens?.accessToken
                ?: return Result.failure(Exception("El servidor no devolvio access token"))
            val user = body.user ?: return Result.failure(Exception("El servidor no devolvio usuario"))
            sessionDataStore.saveSession(accessToken = accessToken, user = user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun requestOtp(action: String): Result<Unit> {
        return try {
            val response = authenticatedAuthService.requestOtp(OtpActionRequest(action = action))
            if (response.isSuccessful) {
                val user = sessionDataStore.userData.first()
                notificationCenter.notify(com.biobox.biotech.domain.notifications.NotificationEvent.OtpSent(user?.nombre ?: "Usuario"))
                Result.success(Unit)
            } else {
                Result.failure(Exception(parseAuthError(response.errorBody()?.string(), "No fue posible solicitar el codigo")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun verifyOtp(action: String, code: String): Result<Unit> {
        return try {
            val response = authenticatedAuthService.verifyOtp(OtpActionRequest(action = action, code = code))
            if (response.isSuccessful) {
                sessionDataStore.updateReAuthTime()
                val user = sessionDataStore.userData.first()
                notificationCenter.notify(com.biobox.biotech.domain.notifications.NotificationEvent.OtpVerified(user?.nombre ?: "Usuario"))
                Result.success(Unit)
            } else {
                Result.failure(Exception(parseAuthError(response.errorBody()?.string(), "Codigo incorrecto")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTelegramStatus() = try {
        val response = authenticatedAuthService.getTelegramStatus()
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception(parseAuthError(response.errorBody()?.string(), "Error al obtener estado de Telegram")))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getLinkingCode() = try {
        val response = authenticatedAuthService.getLinkingCode()
        if (response.isSuccessful && response.body() != null) {
            Result.success(response.body()!!)
        } else {
            Result.failure(Exception(parseAuthError(response.errorBody()?.string(), "Error al obtener codigo de vinculacion")))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun unlinkTelegram(): Result<Unit> {
        return try {
            val response = authenticatedAuthService.unlinkTelegram()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(parseAuthError(response.errorBody()?.string(), "Error al desvincular Telegram")))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun requestPasswordRecovery(phoneNumber: String): Result<Unit> {
        return try {
            val response = authService.requestPasswordRecovery(PasswordRecoveryRequest(phoneNumber))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception(parseAuthError(response.errorBody()?.string(), "No fue posible iniciar la recuperacion de contrasena")))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun confirmPasswordRecovery(phoneNumber: String, code: String, newPassword: String): Result<Unit> {
        return try {
            val response = authService.confirmPasswordRecovery(
                PasswordRecoveryConfirmRequest(
                    phoneNumber = phoneNumber,
                    code = code,
                    newPassword = newPassword
                )
            )
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception(parseAuthError(response.errorBody()?.string(), "No fue posible restablecer la contrasena")))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseAuthError(raw: String?, fallback: String): String {
        if (raw.isNullOrBlank()) return fallback
        val message = Regex("""\"(?:error|message|mensaje)\"\s*:\s*\"([^\"]*)\"""")
            .find(raw)
            ?.groupValues
            ?.getOrNull(1)
            ?: raw.takeUnless { it.trimStart().startsWith("{") }
            ?: fallback
        val normalized = message.lowercase()
        return when {
            normalized.contains("token requerido") || normalized.contains("session_id") -> "La sesión ha expirado. Vuelve a iniciar sesión."
            normalized.contains("código inválido") || normalized.contains("codigo invalido") -> "Código no válido o expirado."
            normalized.contains("expirado") -> "Código no válido o expirado."
            else -> message.ifBlank { fallback }
        }
    }
}

