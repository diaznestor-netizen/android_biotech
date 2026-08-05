package com.biobox.biotech.data.remote.api

import com.biobox.biotech.data.remote.dto.CreateProjectRequest
import com.biobox.biotech.data.remote.dto.DeleteProjectRequest
import com.biobox.biotech.data.remote.dto.ProjectDto
import com.biobox.biotech.data.remote.dto.UpdateProjectRequest
import retrofit2.Response
import retrofit2.http.*

interface ProjectService {
    @GET("projects")
    suspend fun getProjects(@Query("q") query: String? = null): Response<List<ProjectDto>>

    @GET("projects/{id}")
    suspend fun getProjectById(@Path("id") id: Int): Response<ProjectDto>

    @POST("projects")
    suspend fun createProject(
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body request: CreateProjectRequest
    ): Response<ProjectDto>

    @PUT("projects/{id}")
    suspend fun updateProject(@Path("id") id: Int, @Body request: UpdateProjectRequest): Response<ProjectDto>

    @HTTP(method = "DELETE", path = "projects/{id}", hasBody = true)
    suspend fun deleteProject(
        @Path("id") id: Int,
        @Body request: DeleteProjectRequest
    ): Response<ProjectDto>
}
