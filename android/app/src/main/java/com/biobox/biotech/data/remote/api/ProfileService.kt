package com.biobox.biotech.data.remote.api

import com.biobox.biotech.data.remote.dto.ChangePasswordRequest
import com.biobox.biotech.data.remote.dto.ChangePhoneRequest
import com.biobox.biotech.data.remote.dto.UpdateProfileRequest
import com.biobox.biotech.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface ProfileService {
    @GET("profile")
    suspend fun getProfile(): Response<UserDto>

    @PUT("profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UserDto>

    @PUT("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Unit>

    @PUT("auth/change-phone")
    suspend fun changePhone(@Body request: ChangePhoneRequest): Response<Unit>
}
