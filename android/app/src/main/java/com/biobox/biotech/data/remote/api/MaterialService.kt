package com.biobox.biotech.data.remote.api

import com.biobox.biotech.data.remote.dto.MaterialDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface MaterialService {
    @GET("materials")
    suspend fun getMaterials(@Query("search") search: String? = null): Response<List<MaterialDto>>
}
