package com.biobox.biotech.data.mapper

import com.biobox.biotech.data.local.entity.ActivityEntity
import com.biobox.biotech.data.remote.dto.ActivityDto
import com.biobox.biotech.domain.model.Activity
import com.biobox.biotech.domain.model.ActivityStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Locale

private val gson = Gson()

fun ActivityDto.toEntity(): ActivityEntity {
    return ActivityEntity(
        id = id ?: 0,
        titulo = titulo ?: "Actividad sin título",
        descripcion = descripcion,
        responsable = responsable ?: "Sin responsable",
        maquinaId = maquinaId,
        maquinaNombre = maquinaNombre,
        tiempoEmpleado = tiempoEmpleado?.toIntOrNull() ?: 0,
        fecha = fecha.toMillis(),
        evidenciasJson = gson.toJson(evidencias ?: emptyList<String>()),
        comentarios = comentarios,
        estado = estado ?: "PENDIENTE",
        createdAt = createdAt.toMillis()
    )
}

private fun String?.toMillis(): Long = this?.let { value ->
    runCatching { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT).parse(value)?.time }.getOrNull()
} ?: System.currentTimeMillis()

fun ActivityEntity.toDomain(): Activity {
    val type = object : TypeToken<List<String>>() {}.type
    val evidencias: List<String> = try {
        gson.fromJson(evidenciasJson, type) ?: emptyList()
    } catch (_: Exception) { emptyList() }

    return Activity(
        id = id,
        titulo = titulo,
        descripcion = descripcion,
        responsable = responsable,
        maquinaId = maquinaId,
        maquinaNombre = maquinaNombre,
        tiempoEmpleado = tiempoEmpleado,
        fecha = fecha,
        evidencias = evidencias,
        comentarios = comentarios,
        estado = ActivityStatus.fromString(estado),
        createdAt = createdAt
    )
}

fun Activity.toEntity(): ActivityEntity {
    return ActivityEntity(
        id = id,
        titulo = titulo,
        descripcion = descripcion,
        responsable = responsable,
        maquinaId = maquinaId,
        maquinaNombre = maquinaNombre,
        tiempoEmpleado = tiempoEmpleado,
        fecha = fecha,
        evidenciasJson = gson.toJson(evidencias),
        comentarios = comentarios,
        estado = estado.name,
        createdAt = createdAt
    )
}
