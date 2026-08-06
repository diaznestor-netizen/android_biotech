package com.biobox.biotech.data.remote.dto

data class IncidentDto(
    val id: Int?,
    val titulo: String?,
    val descripcion: String?,
    val categoria: String?,
    val gravedad: String?,
    val maquinaId: Int? = null,
    val maquinaNombre: String? = null,
    val reportadoPor: String?,
    val asignadoA: String? = null,
    val fechaReporte: Long? = null,
    val fechaResolucion: Long? = null,
    val estado: String?,
    val evidencias: List<String>? = emptyList(),
    val comentarios: String? = null
)

data class IncidentRequest(
    val titulo: String,
    val descripcion: String,
    val categoria: String,
    val gravedad: String,
    val maquinaId: Int? = null,
    val asignadoA: String? = null
)

data class ResolveRequest(
    val comentarios: String
)
