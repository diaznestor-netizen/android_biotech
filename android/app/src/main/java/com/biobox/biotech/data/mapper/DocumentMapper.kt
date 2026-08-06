package com.biobox.biotech.data.mapper

import com.biobox.biotech.data.local.entity.DocumentEntity
import com.biobox.biotech.data.remote.dto.DocumentDto
import com.biobox.biotech.domain.model.Document
import com.biobox.biotech.domain.model.DocumentType

fun DocumentDto.toEntity(): DocumentEntity {
    return DocumentEntity(
        id = id ?: 0,
        titulo = titulo ?: "Documento sin título",
        tipo = tipo ?: "OTRO",
        maquinaId = maquinaId,
        proyecto = proyecto,
        archivoUrl = archivoUrl ?: "",
        tamano = tamano ?: 0,
        mimeType = mimeType,
        fechaSubida = fechaSubida ?: System.currentTimeMillis(),
        subidoPor = subidoPor
    )
}

fun DocumentEntity.toDomain(): Document {
    return Document(
        id = id,
        titulo = titulo,
        tipo = DocumentType.fromString(tipo),
        maquinaId = maquinaId,
        proyecto = proyecto,
        archivoUrl = archivoUrl,
        tamano = tamano,
        mimeType = mimeType,
        fechaSubida = fechaSubida,
        subidoPor = subidoPor
    )
}
