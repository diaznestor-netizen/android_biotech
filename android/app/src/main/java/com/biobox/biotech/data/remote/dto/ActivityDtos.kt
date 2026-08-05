package com.biobox.biotech.data.remote.dto

data class ActivityDto(
    val id: Int?,
    val titulo: String?,
    val descripcion: String? = null,
    val responsable: String?,
    val maquinaId: Int? = null,
    val maquinaNombre: String? = null,
    val tiempoEmpleado: Int?,
    val fecha: Long?,
    val evidencias: List<String>? = emptyList(),
    val comentarios: String? = null,
    val estado: String?,
    val createdAt: Long? = null
)

data class ActivityRequest(
    val titulo: String,
    val descripcion: String? = null,
    val responsable: String,
    val maquinaId: Int? = null,
    val tiempoEmpleado: Int,
    val fecha: Long,
    val comentarios: String? = null
)

data class ApproveRejectRequest(
    val accion: String,
    val motivo: String? = null
)
