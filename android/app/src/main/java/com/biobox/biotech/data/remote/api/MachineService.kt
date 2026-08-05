package com.biobox.biotech.data.remote.api

import com.biobox.biotech.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface MachineService {
    @GET("production/machines")
    suspend fun getProductionMachines(): Response<List<MachineDto>>

    @GET("production/machines/{id}")
    suspend fun getProductionMachine(@Path("id") id: Int): Response<MachineDto>

    @GET("production/machines/{id}/components")
    suspend fun getProductionComponents(@Path("id") id: Int): Response<List<ComponenteDto>>

    @GET("production/machines/{id}/completion-check")
    suspend fun getCompletionCheck(@Path("id") id: Int): Response<CompletionCheckDto>

    @PUT("production/machines/{id}/components/{componentId}")
    suspend fun updateComponent(
        @Path("id") machineId: Int,
        @Path("componentId") componentId: Int,
        @Body request: UpdateComponentStateRequest
    ): Response<CompletionCheckDto>

    @POST("production/machines/{id}/state")
    suspend fun transitionState(
        @Path("id") machineId: Int,
        @Body request: TransitionMachineStateRequest
    ): Response<Map<String, Any>>

    // Kept for the still-unmigrated legacy creation flow.
    @POST("machines")
    suspend fun createMachine(@Body request: com.biobox.biotech.data.remote.dto.CreateMachineRequest): Response<Map<String, Int>>
}