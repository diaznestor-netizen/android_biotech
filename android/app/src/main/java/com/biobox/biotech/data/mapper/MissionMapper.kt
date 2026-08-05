package com.biobox.biotech.data.mapper

import com.biobox.biotech.data.local.entity.MissionEntity
import com.biobox.biotech.data.remote.dto.MissionDto
import com.biobox.biotech.domain.model.Mission
import com.biobox.biotech.domain.model.MissionPriority
import com.biobox.biotech.domain.model.MissionStatus

fun MissionDto.toEntity(): MissionEntity {
    return MissionEntity(
        id = id ?: 0,
        titulo = titulo ?: "Misión sin título",
        descripcion = descripcion,
        asignadoA = asignadoA ?: "Sin asignar",
        asignadoPor = asignadoPor,
        maquinaId = maquinaId,
        fechaLimite = fechaLimite ?: System.currentTimeMillis(),
        prioridad = prioridad ?: "MEDIA",
        estado = estado ?: "PENDIENTE",
        fechaCreacion = fechaCreacion ?: System.currentTimeMillis(),
        fechaCumplimiento = fechaCumplimiento,
        observaciones = observaciones
    )
}

fun MissionEntity.toDomain(): Mission {
    return Mission(
        id = id,
        titulo = titulo,
        descripcion = descripcion,
        asignadoA = asignadoA,
        asignadoPor = asignadoPor,
        maquinaId = maquinaId,
        fechaLimite = fechaLimite,
        prioridad = MissionPriority.fromString(prioridad),
        estado = MissionStatus.fromString(estado),
        fechaCreacion = fechaCreacion,
        fechaCumplimiento = fechaCumplimiento,
        observaciones = observaciones
    )
}

fun Mission.toEntity(): MissionEntity {
    return MissionEntity(
        id = id,
        titulo = titulo,
        descripcion = descripcion,
        asignadoA = asignadoA,
        asignadoPor = asignadoPor,
        maquinaId = maquinaId,
        fechaLimite = fechaLimite,
        prioridad = prioridad.name,
        estado = estado.name,
        fechaCreacion = fechaCreacion,
        fechaCumplimiento = fechaCumplimiento,
        observaciones = observaciones
    )
}
