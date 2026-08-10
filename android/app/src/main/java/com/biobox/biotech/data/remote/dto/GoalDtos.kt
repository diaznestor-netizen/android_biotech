package com.biobox.biotech.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GoalDto(
    val id: Int?,
    val titulo: String?,
    val descripcion: String? = null,
    val proyecto: String? = null,
    @SerializedName("id_maquina") val maquinaId: Int? = null,
    val porcentajeAvance: Int? = 0,
    @SerializedName("fecha_inicio") val fechaInicio: String?,
    @SerializedName("fecha_fin") val fechaFin: String? = null,
    val estado: String?,
    val actividadesCompletadas: Int? = 0,
    val actividadesTotales: Int? = 0
)

data class GoalRequest(
    val titulo: String,
    val descripcion: String? = null,
    val proyecto: String? = null,
    @SerializedName("id_maquina") val maquinaId: Int? = null,
    @SerializedName("fecha_inicio") val fechaInicio: String,
    @SerializedName("fecha_fin") val fechaFin: String? = null
)
