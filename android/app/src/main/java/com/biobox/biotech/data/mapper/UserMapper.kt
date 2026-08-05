package com.biobox.biotech.data.mapper

import com.biobox.biotech.data.local.entity.UserEntity
import com.biobox.biotech.data.remote.dto.UserDto
import com.biobox.biotech.domain.model.User
import com.biobox.biotech.domain.model.UserRole

fun UserDto.toEntity(): UserEntity {
    return UserEntity(
        id = id?.toString() ?: "",
        nombre = nombre ?: "",
        apellido = apellido ?: "",
        email = email ?: phoneNumber ?: "",
        rol = rol ?: "OPERADOR",
        activo = activo ?: true
    )
}

fun UserEntity.toDomain(): User {
    return User(
        id = id,
        nombre = nombre,
        apellido = apellido,
        email = email,
        rol = UserRole.fromString(rol)
    )
}
