package com.biobox.biotech.data.repository

import com.biobox.biotech.data.local.dao.UserDao
import com.biobox.biotech.data.mapper.toDomain
import com.biobox.biotech.data.mapper.toEntity
import com.biobox.biotech.data.remote.api.UserService
import com.biobox.biotech.data.remote.dto.CreateUserRequest
import com.biobox.biotech.data.remote.dto.UpdateUserRequest
import com.biobox.biotech.domain.model.User
import com.biobox.biotech.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userService: UserService,
    private val userDao: UserDao
) : UserRepository {

    private val _roles = MutableStateFlow<List<String>>(emptyList())

    override fun getUsers(): Flow<List<User>> {
        return userDao.getAllUsers().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun refreshUsers() {
        try {
            val response = userService.getUsers()
            if (response.isSuccessful) {
                val users = response.body().orEmpty().map { it.toEntity() }
                userDao.insertUsers(users)
            }
        } catch (_: Exception) { }
    }

    override suspend fun createUser(user: User, password: String): Result<User> = runCatching {
        val request = CreateUserRequest(
            nombre = user.nombre,
            apellido = user.apellido,
            email = user.email,
            password = password,
            rol = user.rol.name
        )
        val response = userService.createUser(request)
        if (response.isSuccessful) {
            val dto = response.body() ?: throw Exception("Respuesta vacía")
            dto.toEntity().toDomain()
        } else throw Exception("Error al crear usuario: ${response.code()}")
    }

    override suspend fun updateUser(user: User): Result<User> = runCatching {
        val request = UpdateUserRequest(
            nombre = user.nombre,
            apellido = user.apellido,
            email = user.email,
            rol = user.rol.name
        )
        val response = userService.updateUser(user.id, request)
        if (response.isSuccessful) {
            val dto = response.body() ?: throw Exception("Respuesta vacía")
            dto.toEntity().toDomain()
        } else throw Exception("Error al actualizar: ${response.code()}")
    }

    override suspend fun toggleUserActive(id: String, active: Boolean): Result<Unit> = runCatching {
        val response = userService.toggleUserActive(id)
        if (!response.isSuccessful) throw Exception("Error al cambiar estado: ${response.code()}")
        refreshUsers()
    }

    override fun getRoles(): Flow<List<String>> = _roles.asStateFlow()

    suspend fun refreshRoles() {
        try {
            val response = userService.getRoles()
            if (response.isSuccessful) {
                _roles.value = response.body().orEmpty().map { it.nombre }
            }
        } catch (_: Exception) { }
    }
}
