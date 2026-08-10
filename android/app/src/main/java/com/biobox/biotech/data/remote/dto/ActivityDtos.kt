package com.biobox.biotech.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ActivityDto(
    val id: Int?,
    val titulo: String?,
    val descripcion: String? = null,
    val responsable: String?,
    @SerializedName("id_maquina") val maquinaId: Int? = null,
    val maquinaNombre: String? = null,
    @SerializedName("tiempo_empleado") val tiempoEmpleado: String?,
    val fecha: String?,
    val evidencias: List<String>? = emptyList(),
    val comentarios: String? = null,
    val estado: String?,
    @SerializedName("created_at") val createdAt: String? = null
)

data class ActivityRequest(
    val titulo: String,
    val descripcion: String? = null,
    val responsable: String,
    @SerializedName("id_maquina") val maquinaId: Int? = null,
    @SerializedName("tiempo_empleado") val tiempoEmpleado: String,
    val fecha: String,
    val comentarios: String? = null
)

data class ApproveRejectRequest(
    val accion: String,
    val motivo: String? = null
)
