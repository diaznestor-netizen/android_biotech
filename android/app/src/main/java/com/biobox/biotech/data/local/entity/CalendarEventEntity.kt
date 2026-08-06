package com.biobox.biotech.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_events")
data class CalendarEventEntity(
    @PrimaryKey val id: Int,
    val titulo: String,
    val descripcion: String? = null,
    val tipo: String,
    val fechaInicio: Long,
    val fechaFin: Long? = null,
    val todoElDia: Boolean = false,
    val maquinaId: Int? = null,
    val proyecto: String? = null,
    val creadoPor: String? = null,
    val color: String? = null
)
