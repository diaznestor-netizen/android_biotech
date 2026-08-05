package com.biobox.biotech.data.remote.dto

data class GoalDto(
    val id: Int?,
    val titulo: String?,
    val descripcion: String? = null,
    val proyecto: String? = null,
    val maquinaId: Int? = null,
    val porcentajeAvance: Int? = 0,
    val fechaInicio: Long?,
    val fechaFin: Long? = null,
    val estado: String?,
    val actividadesCompletadas: Int? = 0,
    val actividadesTotales: Int? = 0
)

data class GoalRequest(
    val titulo: String,
    val descripcion: String? = null,
    val proyecto: String? = null,
    val maquinaId: Int? = null,
    val fechaInicio: Long,
    val fechaFin: Long? = null
)
