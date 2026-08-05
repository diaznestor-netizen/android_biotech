package com.biobox.biotech.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey val id: Int,
    val titulo: String,
    val descripcion: String? = null,
    val responsable: String,
    val maquinaId: Int? = null,
    val maquinaNombre: String? = null,
    val tiempoEmpleado: Int,
    val fecha: Long,
    val evidenciasJson: String = "[]",
    val comentarios: String? = null,
    val estado: String,
    val createdAt: Long
)
