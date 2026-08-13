package com.biobox.biotech.domain.repository

import com.biobox.biotech.domain.model.User

interface ProfileRepository {
    suspend fun getProfile(): Result<User>
    suspend fun updateProfile(nombre: String, apellido: String, email: String): Result<User>
    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit>
    suspend fun changePhone(newPhone: String): Result<Unit>
}
