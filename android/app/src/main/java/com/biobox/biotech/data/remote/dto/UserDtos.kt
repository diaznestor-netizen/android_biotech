package com.biobox.biotech.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserDto(
    val id: Int?,
    val nombre: String?,
    val apellido: String?,
    val email: String? = null,
    @SerializedName("phone_number") val phoneNumber: String? = null,
    val rol: String?,
    val activo: Boolean? = true
)

data class CreateUserRequest(
    val nombre: String,
    val apellido: String,
    val email: String,
    val password: String,
    val rol: String
)

data class UpdateUserRequest(
    val nombre: String? = null,
    val apellido: String? = null,
    val email: String? = null,
    val rol: String? = null
)

data class RoleDto(
    val id: Int,
    val nombre: String,
    val descripcion: String? = null
)
