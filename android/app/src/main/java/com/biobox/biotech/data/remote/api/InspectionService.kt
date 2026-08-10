package com.biobox.biotech.data.remote.api

import com.biobox.biotech.data.remote.dto.InspectionListDto
import com.biobox.biotech.data.remote.dto.EvidenceUploadResponse
import com.biobox.biotech.data.remote.dto.InspectionRequest
import com.biobox.biotech.data.remote.dto.InspectionResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface InspectionService {
    @GET("inspections")
    suspend fun getInspections(): Response<List<InspectionListDto>>

    @POST("inspections")
    suspend fun submitInspection(@Body request: InspectionRequest): Response<InspectionResponse>

    @Multipart
    @POST("evidence")
    suspend fun uploadEvidence(
        @Part("id_revision") inspectionId: Int,
        @Part file: MultipartBody.Part
    ): Response<EvidenceUploadResponse>
}
