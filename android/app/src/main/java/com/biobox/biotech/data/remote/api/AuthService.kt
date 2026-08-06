package com.biobox.biotech.data.remote.api

import com.biobox.biotech.data.remote.dto.AuthMessageResponse
import com.biobox.biotech.data.remote.dto.LoginRequest
import com.biobox.biotech.data.remote.dto.LoginResponse
import com.biobox.biotech.data.remote.dto.OtpActionRequest
import com.biobox.biotech.data.remote.dto.PasswordRecoveryConfirmRequest
import com.biobox.biotech.data.remote.dto.PasswordRecoveryRequest
import com.biobox.biotech.data.remote.dto.TelegramLinkCodeResponse
import com.biobox.biotech.data.remote.dto.TelegramStatusResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/refresh")
    suspend fun refresh(): Response<LoginResponse>

    @POST("auth/verify-2fa")
    suspend fun verifySecondFactor(@Body request: Map<String, String>): Response<LoginResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    @POST("auth/request-otp")
    suspend fun requestOtp(@Body request: OtpActionRequest): Response<AuthMessageResponse>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: OtpActionRequest): Response<AuthMessageResponse>

    @GET("auth/telegram-status")
    suspend fun getTelegramStatus(): Response<TelegramStatusResponse>

    @POST("auth/link-telegram")
    suspend fun getLinkingCode(): Response<TelegramLinkCodeResponse>

    @DELETE("auth/unlink-telegram")
    suspend fun unlinkTelegram(): Response<AuthMessageResponse>

    @POST("auth/password-recovery/request")
    suspend fun requestPasswordRecovery(@Body request: PasswordRecoveryRequest): Response<AuthMessageResponse>

    @POST("auth/password-recovery/confirm")
    suspend fun confirmPasswordRecovery(@Body request: PasswordRecoveryConfirmRequest): Response<AuthMessageResponse>
}
