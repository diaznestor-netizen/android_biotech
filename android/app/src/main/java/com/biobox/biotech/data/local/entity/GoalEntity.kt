package com.biobox.biotech.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: Int,
    val titulo: String,
    val descripcion: String? = null,
    val proyecto: String? = null,
    val maquinaId: Int? = null,
    val porcentajeAvance: Int = 0,
    val fechaInicio: Long,
    val fechaFin: Long? = null,
    val estado: String,
    val actividadesCompletadas: Int = 0,
    val actividadesTotales: Int = 0
)
