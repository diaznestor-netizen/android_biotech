package com.biobox.biotech.data.remote.api

import com.biobox.biotech.data.remote.dto.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ActivityService {
    @GET("activities")
    suspend fun getActivities(): Response<List<ActivityDto>>

    @GET("activities/{id}")
    suspend fun getActivityById(@Path("id") id: Int): Response<ActivityDto>

    @POST("activities")
    suspend fun createActivity(@Body request: ActivityRequest): Response<ActivityDto>

    @Multipart
    @POST("activities/{id}/evidence")
    suspend fun uploadEvidence(
        @Path("id") id: Int,
        @Part file: MultipartBody.Part
    ): Response<EvidenceUploadResponse>

    @PUT("activities/{id}")
    suspend fun updateActivity(@Path("id") id: Int, @Body request: ActivityRequest): Response<ActivityDto>

    @POST("activities/{id}/approve")
    suspend fun approveActivity(@Path("id") id: Int): Response<Unit>

    @POST("activities/{id}/reject")
    suspend fun rejectActivity(@Path("id") id: Int, @Body request: ApproveRejectRequest): Response<Unit>
}
