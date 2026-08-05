package com.biobox.biotech.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.biobox.biotech.core.common.SyncStatus

@Entity(tableName = "materials")
data class MaterialEntity(
    @PrimaryKey val localId: String,
    val remoteId: Int?,
    val codigo: String,
    val nombre: String,
    val cantidadRequerida: Int,
    val cantidadDisponible: Int,
    val estado: String,
    val descripcion: String?,
    val unidad: String?,
    val stockMin: Double,
    val activo: Boolean,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val updatedAt: Long = System.currentTimeMillis()
)
