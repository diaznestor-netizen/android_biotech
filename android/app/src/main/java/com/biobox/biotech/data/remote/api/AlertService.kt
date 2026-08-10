package com.biobox.biotech.data.remote.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

data class IncidentAlertDto(
    val id: Int,
    val incidenciaId: Int,
    val codigoMaquina: String,
    val gravedad: String,
    val mensaje: String,
    val reconocida: Boolean,
    val fechaCreacion: Long
)

data class IncidentAlertsDto(val count: Int = 0, val alerts: List<IncidentAlertDto> = emptyList())

interface AlertService {
    @GET("alerts/unacknowledged")
    suspend fun getUnacknowledged(@Query("limit") limit: Int = 20): Response<IncidentAlertsDto>

    @PUT("alerts/{id}/acknowledge")
    suspend fun acknowledge(@Path("id") id: Int): Response<Unit>
}
