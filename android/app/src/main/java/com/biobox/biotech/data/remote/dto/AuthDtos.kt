package com.biobox.biotech.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val phoneNumber: String,
    val password: String
)

data class RegisterRequest(
    val phoneNumber: String,
    val password: String,
    val nombre: String,
    val apellido: String,
    val email: String
)

data class LoginResponse(
    val tokens: Tokens? = null,
    val user: UserDto? = null,
    @SerializedName("mensaje") val message: String? = null
)

data class Tokens(
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("expires_in") val expiresIn: Long? = null
)
