package com.biobox.biotech.data.remote.dto

data class DocumentDto(
    val id: Int?,
    val titulo: String?,
    val tipo: String?,
    val maquinaId: Int? = null,
    val proyecto: String? = null,
    val archivoUrl: String?,
    val tamano: Long? = 0,
    val mimeType: String? = null,
    val fechaSubida: Long? = null,
    val subidoPor: String? = null
)
