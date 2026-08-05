package com.biobox.biotech.data.mapper

import com.biobox.biotech.data.local.entity.CalendarEventEntity
import com.biobox.biotech.data.remote.dto.CalendarEventDto
import com.biobox.biotech.domain.model.CalendarEvent
import com.biobox.biotech.domain.model.EventType

fun CalendarEventDto.toEntity(): CalendarEventEntity {
    return CalendarEventEntity(
        id = id ?: 0,
        titulo = titulo ?: "Evento sin título",
        descripcion = descripcion,
        tipo = tipo ?: "OTRO",
        fechaInicio = fechaInicio ?: System.currentTimeMillis(),
        fechaFin = fechaFin,
        todoElDia = todoElDia ?: false,
        maquinaId = maquinaId,
        proyecto = proyecto,
        creadoPor = creadoPor,
        color = color
    )
}

fun CalendarEventEntity.toDomain(): CalendarEvent {
    return CalendarEvent(
        id = id,
        titulo = titulo,
        descripcion = descripcion,
        tipo = EventType.fromString(tipo),
        fechaInicio = fechaInicio,
        fechaFin = fechaFin,
        todoElDia = todoElDia,
        maquinaId = maquinaId,
        proyecto = proyecto,
        creadoPor = creadoPor,
        color = color
    )
}
