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
import com.biobox.biotech.data.remote.dto.RegisterRequest
import com.biobox.biotech.core.util.safeApiCall
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

    override suspend fun register(phoneNumber: String, password: String, nombre: String, apellido: String, email: String): Result<User> {
        val phoneMasked = SecurityUtils.ofuscarTelefono(phoneNumber)
        Log.d(AppConstants.TAG_AUTH, "Registrando cuenta para: $phoneMasked")

        return safeApiCall(
            apiCall = { authService.register(RegisterRequest(phoneNumber, password, nombre, apellido, email)) },
            transform = { it }
        ).mapCatching { body ->
            persistSession(body)
        }.onFailure { error ->
            Log.e(AppConstants.TAG_AUTH, "Error en registro ($phoneMasked): ${error.message}")
        }
    }

    override suspend fun login(phoneNumber: String, password: String): Result<User> {
        val phoneMasked = SecurityUtils.ofuscarTelefono(phoneNumber)
        Log.d(AppConstants.TAG_AUTH, "Iniciando autenticación para: $phoneMasked")

        return safeApiCall(
            apiCall = { authService.login(LoginRequest(phoneNumber, password)) },
            transform = { it }
        ).mapCatching { body ->
            persistSession(body)
        }.onFailure { error ->
            Log.e(AppConstants.TAG_AUTH, "Error en login ($phoneMasked): ${error.message}")
        }
    }

    override suspend fun reauthenticate(password: String): Result<User> {
        return try {
            val response = authenticatedAuthService.reauthenticate(mapOf("password" to password))
            if (!response.isSuccessful) {
                return Result.failure(Exception(parseAuthError(response.errorBody()?.string(), "Contraseña incorrecta")))
            }
            val body = response.body() ?: return Result.failure(Exception("Respuesta vacía del servidor"))
            Result.success(persistSession(body))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun persistSession(body: com.biobox.biotech.data.remote.dto.LoginResponse): User {
        val accessToken = body.tokens?.accessToken
            ?: throw Exception("El servidor no devolvió access token")
        val user = body.user ?: throw Exception("El servidor no devolvió usuario")
        sessionDataStore.saveSession(accessToken = accessToken, user = user)
        return user.toDomain()
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
                return Result.failure(Exception("Tu sesión expiró. Inicia sesión nuevamente."))
            }
            val body = response.body() ?: return Result.failure(Exception("Respuesta vacía del servidor"))
            persistSession(body)
            Result.success(Unit)
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
            normalized.contains("contraseña incorrecta") || normalized.contains("credenciales") -> "Teléfono o contraseña incorrectos."
            else -> message.ifBlank { fallback }
        }
    }
}