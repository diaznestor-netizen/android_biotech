package com.biobox.biotech.data.remote.api

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

data class HistoryEntryDto(
    val fecha: String,
    val origen: String,
    val usuario: String,
    val entidad: String,
    val registro: String,
    val accion: String,
    val anterior: String,
    val nuevo: String,
    val detalle: String
)

data class HistoryPageDto(
    val items: List<HistoryEntryDto> = emptyList(),
    val page: Int = 1,
    val limit: Int = 50,
    val total: Int = 0
)

interface ReportService {
    @GET("reports/biotech.xlsx")
    suspend fun getGlobalExcel(): Response<ResponseBody>

    @GET("reports/history")
    suspend fun getGlobalHistory(@Query("page") page: Int = 1, @Query("limit") limit: Int = 100): Response<HistoryPageDto>
}
