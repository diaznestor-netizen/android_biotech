package com.biobox.biotech.data.remote.dto

data class MissionDto(
    val id: Int?,
    val titulo: String?,
    val descripcion: String? = null,
    val asignadoA: String?,
    val asignadoPor: String? = null,
    val maquinaId: Int? = null,
    val fechaLimite: Long?,
    val prioridad: String?,
    val estado: String?,
    val fechaCreacion: Long? = null,
    val fechaCumplimiento: Long? = null,
    val observaciones: String? = null
)

data class MissionRequest(
    val titulo: String,
    val descripcion: String? = null,
    val asignadoA: String,
    val maquinaId: Int? = null,
    val fechaLimite: Long,
    val prioridad: String
)

data class CompleteMissionRequest(
    val observaciones: String? = null
)
