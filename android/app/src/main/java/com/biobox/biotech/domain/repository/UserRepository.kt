package com.biobox.biotech.domain.repository

import com.biobox.biotech.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUsers(): Flow<List<User>>
    suspend fun refreshUsers()
    suspend fun createUser(user: User, password: String): Result<User>
    suspend fun updateUser(user: User): Result<User>
    suspend fun toggleUserActive(id: String, active: Boolean): Result<Unit>
    fun getRoles(): Flow<List<String>>
}
