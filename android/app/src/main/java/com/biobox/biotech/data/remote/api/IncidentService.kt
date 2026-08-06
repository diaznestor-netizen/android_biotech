package com.biobox.biotech.data.remote.api

import com.biobox.biotech.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface IncidentService {
    @GET("incidents")
    suspend fun getIncidents(): Response<List<IncidentDto>>

    @GET("incidents/{id}")
    suspend fun getIncidentById(@Path("id") id: Int): Response<IncidentDto>

    @POST("incidents")
    suspend fun createIncident(@Body request: IncidentRequest): Response<IncidentDto>

    @PUT("incidents/{id}")
    suspend fun updateIncident(@Path("id") id: Int, @Body request: IncidentRequest): Response<IncidentDto>

    @POST("incidents/{id}/resolve")
    suspend fun resolveIncident(@Path("id") id: Int, @Body request: ResolveRequest): Response<Unit>
}
