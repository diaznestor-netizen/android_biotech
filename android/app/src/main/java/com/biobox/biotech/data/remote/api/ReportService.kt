package com.biobox.biotech.data.remote.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ReportService {
    @GET("reports/inventory/pdf")
    suspend fun getInventoryPdf(): Response<ResponseBody>

    @GET("reports/inventory/csv")
    suspend fun getInventoryCsv(): Response<ResponseBody>

    @GET("reports/machines/pdf")
    suspend fun getMachinesPdf(): Response<ResponseBody>

    @GET("reports/machines/csv")
    suspend fun getMachinesCsv(): Response<ResponseBody>

    @GET("reports/activities/pdf")
    suspend fun getActivitiesPdf(): Response<ResponseBody>

    @GET("reports/activities/csv")
    suspend fun getActivitiesCsv(): Response<ResponseBody>

    @GET("reports/missions/pdf")
    suspend fun getMissionsPdf(): Response<ResponseBody>

    @GET("reports/missions/csv")
    suspend fun getMissionsCsv(): Response<ResponseBody>

    @GET("reports/incidents/pdf")
    suspend fun getIncidentsPdf(): Response<ResponseBody>

    @GET("reports/incidents/csv")
    suspend fun getIncidentsCsv(): Response<ResponseBody>

    @GET("reports/productivity/pdf")
    suspend fun getProductivityPdf(): Response<ResponseBody>

    @GET("reports/productivity/csv")
    suspend fun getProductivityCsv(): Response<ResponseBody>
}
