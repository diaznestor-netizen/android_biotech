package com.biobox.biotech.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "incidents")
data class IncidentEntity(
    @PrimaryKey val id: Int,
    val titulo: String,
    val descripcion: String,
    val categoria: String,
    val gravedad: String,
    val maquinaId: Int? = null,
    val maquinaNombre: String? = null,
    val reportadoPor: String,
    val asignadoA: String? = null,
    val fechaReporte: Long,
    val fechaResolucion: Long? = null,
    val estado: String,
    val evidenciasJson: String = "[]",
    val comentarios: String? = null
)
