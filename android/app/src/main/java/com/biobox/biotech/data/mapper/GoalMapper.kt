package com.biobox.biotech.data.mapper

import com.biobox.biotech.data.local.entity.GoalEntity
import com.biobox.biotech.data.remote.dto.GoalDto
import com.biobox.biotech.domain.model.Goal
import com.biobox.biotech.domain.model.GoalStatus

fun GoalDto.toEntity(): GoalEntity {
    return GoalEntity(
        id = id ?: 0,
        titulo = titulo ?: "Meta sin título",
        descripcion = descripcion,
        proyecto = proyecto,
        maquinaId = maquinaId,
        porcentajeAvance = porcentajeAvance ?: 0,
        fechaInicio = fechaInicio ?: System.currentTimeMillis(),
        fechaFin = fechaFin,
        estado = estado ?: "PENDIENTE",
        actividadesCompletadas = actividadesCompletadas ?: 0,
        actividadesTotales = actividadesTotales ?: 0
    )
}

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

fun Goal.toEntity(): GoalEntity {
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
        actividadesTotales = actividadesTotales
    )
}
