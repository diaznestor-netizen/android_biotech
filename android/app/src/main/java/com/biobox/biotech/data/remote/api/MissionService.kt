package com.biobox.biotech.data.remote.api

import com.biobox.biotech.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface MissionService {
    @GET("missions")
    suspend fun getMissions(): Response<List<MissionDto>>

    @GET("missions/completed")
    suspend fun getCompletedMissions(): Response<List<MissionDto>>

    @GET("missions/{id}")
    suspend fun getMissionById(@Path("id") id: Int): Response<MissionDto>

    @POST("missions")
    suspend fun createMission(@Body request: MissionRequest): Response<MissionDto>

    @PUT("missions/{id}")
    suspend fun updateMission(@Path("id") id: Int, @Body request: MissionRequest): Response<MissionDto>

    @POST("missions/{id}/complete")
    suspend fun completeMission(@Path("id") id: Int, @Body request: CompleteMissionRequest): Response<Unit>

    @POST("missions/{id}/approve")
    suspend fun approveMission(@Path("id") id: Int): Response<Unit>
}
