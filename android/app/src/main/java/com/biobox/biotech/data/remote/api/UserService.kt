package com.biobox.biotech.data.remote.api

import com.biobox.biotech.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface UserService {
    @GET("users")
    suspend fun getUsers(): Response<List<UserDto>>

    @POST("users")
    suspend fun createUser(@Body request: CreateUserRequest): Response<UserDto>

    @PUT("users/{id}")
    suspend fun updateUser(@Path("id") id: String, @Body request: UpdateUserRequest): Response<UserDto>

    @POST("users/{id}/toggle")
    suspend fun toggleUserActive(@Path("id") id: String): Response<Unit>

    @GET("users/roles")
    suspend fun getRoles(): Response<List<RoleDto>>
}
