package com.biobox.biotech.data.mapper

import com.biobox.biotech.data.local.entity.IncidentEntity
import com.biobox.biotech.data.remote.dto.IncidentDto
import com.biobox.biotech.domain.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private val gson = Gson()

fun IncidentDto.toEntity(): IncidentEntity {
    return IncidentEntity(
        id = id ?: 0,
        titulo = titulo ?: "Incidencia sin título",
        descripcion = descripcion ?: "Sin descripción",
        categoria = categoria ?: "OTRA",
        gravedad = gravedad ?: "BAJA",
        maquinaId = maquinaId,
        maquinaNombre = maquinaNombre,
        reportadoPor = reportadoPor ?: "Desconocido",
        asignadoA = asignadoA,
        fechaReporte = fechaReporte ?: System.currentTimeMillis(),
        fechaResolucion = fechaResolucion,
        estado = estado ?: "ABIERTA",
        evidenciasJson = gson.toJson(evidencias ?: emptyList<String>()),
        comentarios = comentarios
    )
}

fun IncidentEntity.toDomain(): Incident {
    val type = object : TypeToken<List<String>>() {}.type
    val evidencias: List<String> = try {
        gson.fromJson(evidenciasJson, type) ?: emptyList()
    } catch (_: Exception) { emptyList() }

    return Incident(
        id = id,
        titulo = titulo,
        descripcion = descripcion,
        categoria = IncidentCategory.fromString(categoria),
        gravedad = IncidentSeverity.fromString(gravedad),
        maquinaId = maquinaId,
        maquinaNombre = maquinaNombre,
        reportadoPor = reportadoPor,
        asignadoA = asignadoA,
        fechaReporte = fechaReporte,
        fechaResolucion = fechaResolucion,
        estado = IncidentStatus.fromString(estado),
        evidencias = evidencias,
        comentarios = comentarios
    )
}

fun Incident.toEntity(): IncidentEntity {
    return IncidentEntity(
        id = id,
        titulo = titulo,
        descripcion = descripcion,
        categoria = categoria.name,
        gravedad = gravedad.name,
        maquinaId = maquinaId,
        maquinaNombre = maquinaNombre,
        reportadoPor = reportadoPor,
        asignadoA = asignadoA,
        fechaReporte = fechaReporte,
        fechaResolucion = fechaResolucion,
        estado = estado.name,
        evidenciasJson = gson.toJson(evidencias),
        comentarios = comentarios
    )
}
