package com.biobox.biotech.domain.repository

import com.biobox.biotech.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    fun getPendingSyncCount(): Flow<Int>
    suspend fun register(phoneNumber: String, password: String, nombre: String, apellido: String, email: String): Result<User>
    suspend fun login(phoneNumber: String, password: String): Result<User>
    suspend fun logout()
    suspend fun checkSession(): Boolean
    suspend fun refreshSession(): Result<Unit>
    suspend fun reauthenticate(password: String): Result<User>
}