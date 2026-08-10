package com.biobox.biotech.data.remote.dto

import com.google.gson.annotations.SerializedName

data class InspectionRequest(
    @SerializedName("id_maquina") val machineId: Int,
    val observaciones: String? = null,
    @SerializedName("detalles") val items: List<InspectionItemRequest>
)

data class InspectionItemRequest(
    @SerializedName("id_material") val materialId: Int,
    @SerializedName("cantidad_verificada") val cantidadEncontrada: Int,
    @SerializedName("cantidad_requerida") val cantidadRequerida: Double = 0.0,
    val estado: String = "Pendiente"
)

data class InspectionResponse(
    val id: Int,
    @SerializedName("estado_general") val status: String? = null,
    val message: String? = null
)

data class EvidenceUploadResponse(
    val id: Int,
    val url: String? = null,
    val mime: String? = null
)

data class InspectionListDto(
    val id: Int,
    @SerializedName("id_maquina") val machineId: Int,
    @SerializedName("maquina_codigo") val machineCode: String,
    @SerializedName("maquina_nombre") val machineName: String,
    @SerializedName("tecnico") val auditor: String,
    @SerializedName("fecha") val date: String,
    @SerializedName("estado") val status: String,
    @SerializedName("porcentaje") val progress: Double,
    @SerializedName("observaciones") val notes: String? = null
)
