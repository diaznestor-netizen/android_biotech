package com.biobox.biotech.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: Int,
    val titulo: String,
    val tipo: String,
    val maquinaId: Int? = null,
    val proyecto: String? = null,
    val archivoUrl: String,
    val tamano: Long = 0,
    val mimeType: String? = null,
    val fechaSubida: Long,
    val subidoPor: String? = null
)
