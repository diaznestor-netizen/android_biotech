package com.biobox.biotech.data.remote.api

import com.biobox.biotech.data.remote.dto.DocumentDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface DocumentService {
    @GET("documents")
    suspend fun getDocuments(): Response<List<DocumentDto>>

    @GET("documents/machine/{machineId}")
    suspend fun getDocumentsByMachine(@Path("machineId") machineId: Int): Response<List<DocumentDto>>

    @Multipart
    @POST("documents/upload")
    suspend fun uploadDocument(
        @Part("titulo") titulo: RequestBody,
        @Part("tipo") tipo: RequestBody,
        @Part("maquinaId") maquinaId: RequestBody? = null,
        @Part file: MultipartBody.Part
    ): Response<DocumentDto>

    @DELETE("documents/{id}")
    suspend fun deleteDocument(@Path("id") id: Int): Response<Unit>
}
