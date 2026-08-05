package com.biobox.biotech.data.mapper

import com.biobox.biotech.data.remote.dto.UserDto
import com.biobox.biotech.domain.model.User
import com.biobox.biotech.domain.model.UserRole

fun UserDto.toDomain(): User {
    return User(
        id = id?.toString() ?: "",
        nombre = nombre ?: "",
        apellido = apellido ?: "",
        email = email ?: phoneNumber ?: "",
        rol = UserRole.fromString(rol ?: "")
    )
}
