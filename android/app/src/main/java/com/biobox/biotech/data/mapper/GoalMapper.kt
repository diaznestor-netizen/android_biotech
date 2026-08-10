package com.biobox.biotech.data.mapper

import com.biobox.biotech.data.local.entity.GoalEntity
import com.biobox.biotech.core.common.SyncStatus
import com.biobox.biotech.data.remote.dto.GoalDto
import com.biobox.biotech.domain.model.Goal
import com.biobox.biotech.domain.model.GoalStatus
import java.text.SimpleDateFormat
import java.util.Locale

fun GoalDto.toEntity(): GoalEntity {
    return GoalEntity(
        id = id ?: 0,
        titulo = titulo ?: "Meta sin título",
        descripcion = descripcion,
        proyecto = proyecto,
        maquinaId = maquinaId,
        porcentajeAvance = porcentajeAvance ?: 0,
        fechaInicio = fechaInicio.toGoalMillis(),
        fechaFin = fechaFin?.toGoalMillis(),
        estado = estado ?: "PENDIENTE",
        actividadesCompletadas = actividadesCompletadas ?: 0,
        actividadesTotales = actividadesTotales ?: 0
    )
}

private fun String?.toGoalMillis(): Long = this?.let { value ->
    runCatching { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ROOT).parse(value)?.time }.getOrNull()
} ?: System.currentTimeMillis()

fun GoalEntity.toDomain(): Goal {
    return Goal(
        id = id,
        titulo = titulo,
        descripcion = descripcion,
        proyecto = proyecto,
        maquinaId = maquinaId,
        porcentajeAvance = porcentajeAvance,
        fechaInicio = fechaInicio,
        fechaFin = fechaFin,
        estado = GoalStatus.fromString(estado),
        actividadesCompletadas = actividadesCompletadas,
        actividadesTotales = actividadesTotales
    )
}

fun Goal.toEntity(syncStatus: SyncStatus = SyncStatus.SYNCED): GoalEntity {
    return GoalEntity(
        id = id,
        titulo = titulo,
        descripcion = descripcion,
        proyecto = proyecto,
        maquinaId = maquinaId,
        porcentajeAvance = porcentajeAvance,
        fechaInicio = fechaInicio,
        fechaFin = fechaFin,
        estado = estado.name,
        actividadesCompletadas = actividadesCompletadas,
        actividadesTotales = actividadesTotales,
        syncStatus = syncStatus
    )
}
