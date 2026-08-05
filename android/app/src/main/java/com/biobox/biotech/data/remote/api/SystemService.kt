package com.biobox.biotech.data.remote.api

import retrofit2.Response
import retrofit2.http.GET

interface SystemService {
    @GET("health")
    suspend fun health(): Response<Unit>
}
