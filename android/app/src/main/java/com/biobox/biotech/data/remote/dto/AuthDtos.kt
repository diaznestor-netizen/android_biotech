package com.biobox.biotech.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val phoneNumber: String,
    val password: String
)

data class PasswordlessLoginRequest(
    val telefono: String,
    val codigo: String
)

data class LoginResponse(
    val tokens: Tokens? = null,
    val user: UserDto? = null,
    @SerializedName("requires_2fa") val requires2FA: Boolean = false,
    @SerializedName("session_id") val sessionId: String? = null,
    @SerializedName("mensaje") val message: String? = null
)

data class OtpActionRequest(
    val action: String,
    val code: String? = null
)

data class PasswordRecoveryRequest(
    val phoneNumber: String
)

data class PasswordRecoveryConfirmRequest(
    val phoneNumber: String,
    val code: String,
    @SerializedName("new_password") val newPassword: String
)

data class AuthMessageResponse(
    @SerializedName("mensaje") val message: String? = null,
    @SerializedName("action") val action: String? = null,
    @SerializedName("error") val error: String? = null
)

data class Tokens(
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("expires_in") val expiresIn: Long? = null
)
