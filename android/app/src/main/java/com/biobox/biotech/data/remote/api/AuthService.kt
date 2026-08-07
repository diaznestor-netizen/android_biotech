package com.biobox.biotech.data.remote.api

import com.biobox.biotech.data.remote.dto.LoginRequest
import com.biobox.biotech.data.remote.dto.LoginResponse
import com.biobox.biotech.data.remote.dto.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/refresh")
    suspend fun refresh(): Response<LoginResponse>

    @POST("auth/reauthenticate")
    suspend fun reauthenticate(@Body request: Map<String, String>): Response<LoginResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>
}
