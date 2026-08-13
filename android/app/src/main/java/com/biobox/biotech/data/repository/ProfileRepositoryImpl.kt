package com.biobox.biotech.data.repository

import com.biobox.biotech.core.datastore.SessionDataStore
import com.biobox.biotech.data.mapper.toDomain
import com.biobox.biotech.data.remote.api.ProfileService
import com.biobox.biotech.data.remote.dto.ChangePasswordRequest
import com.biobox.biotech.data.remote.dto.ChangePhoneRequest
import com.biobox.biotech.data.remote.dto.UpdateProfileRequest
import com.biobox.biotech.domain.model.User
import com.biobox.biotech.domain.repository.ProfileRepository
import com.google.gson.JsonParser
import kotlinx.coroutines.flow.first
import retrofit2.Response
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val profileService: ProfileService,
    private val sessionDataStore: SessionDataStore
) : ProfileRepository {

    override suspend fun getProfile(): Result<User> = runCatching {
        val response = try {
            profileService.getProfile()
        } catch (e: Exception) {
            throw Exception("Sin conexión al consultar el perfil")
        }
        val dto = response.takeIf { it.isSuccessful }?.body()
            ?: throw Exception(errorMessage(response))
        sessionDataStore.updateUser(dto)
        dto.toDomain()
    }

    override suspend fun updateProfile(nombre: String, apellido: String, email: String): Result<User> = runCatching {
        val response = try {
            profileService.updateProfile(
                UpdateProfileRequest(
                    nombre = nombre,
                    apellido = apellido,
                    // La API ignora cadenas vacías: no enviar el campo deja el valor intacto
                    email = email.takeIf { it.isNotBlank() }
                )
            )
        } catch (e: Exception) {
            throw Exception("Sin conexión al actualizar el perfil")
        }
        val dto = response.takeIf { it.isSuccessful }?.body()
            ?: throw Exception(errorMessage(response))
        sessionDataStore.updateUser(dto)
        dto.toDomain()
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = runCatching {
        val response = try {
            profileService.changePassword(ChangePasswordRequest(currentPassword, newPassword))
        } catch (e: Exception) {
            throw Exception("Sin conexión al cambiar la contraseña")
        }
        if (!response.isSuccessful) throw Exception(errorMessage(response))
    }

    override suspend fun changePhone(newPhone: String): Result<Unit> = runCatching {
        val response = try {
            profileService.changePhone(ChangePhoneRequest(newPhone))
        } catch (e: Exception) {
            throw Exception("Sin conexión al cambiar el teléfono")
        }
        if (!response.isSuccessful) throw Exception(errorMessage(response))
        // La API no devuelve el perfil: reflejar el teléfono nuevo en la sesión local
        sessionDataStore.userData.first()?.let { current ->
            sessionDataStore.updateUser(current.copy(phoneNumber = newPhone))
        }
    }

    private fun errorMessage(response: Response<*>): String {
        val body = runCatching { response.errorBody()?.string() }.getOrNull()
        val parsed = body?.let { raw ->
            runCatching { JsonParser.parseString(raw).asJsonObject.get("error")?.asString }.getOrNull()
        }
        return parsed?.takeIf { it.isNotBlank() } ?: "Error HTTP ${response.code()}"
    }
}
