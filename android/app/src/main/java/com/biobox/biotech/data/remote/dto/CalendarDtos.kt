package com.biobox.biotech.data.remote.dto

data class CalendarEventDto(
    val id: Int?,
    val titulo: String?,
    val descripcion: String? = null,
    val tipo: String?,
    val fechaInicio: Long?,
    val fechaFin: Long? = null,
    val todoElDia: Boolean? = false,
    val maquinaId: Int? = null,
    val proyecto: String? = null,
    val creadoPor: String? = null,
    val color: String? = null
)

data class CalendarEventRequest(
    val titulo: String,
    val descripcion: String? = null,
    val tipo: String,
    val fechaInicio: Long,
    val fechaFin: Long? = null,
    val todoElDia: Boolean = false,
    val maquinaId: Int? = null,
    val proyecto: String? = null
)
