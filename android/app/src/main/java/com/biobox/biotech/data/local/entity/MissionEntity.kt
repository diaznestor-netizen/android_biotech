package com.biobox.biotech.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.biobox.biotech.core.common.SyncStatus

@Entity(tableName = "missions")
data class MissionEntity(
    @PrimaryKey val id: Int,
    val titulo: String,
    val descripcion: String? = null,
    val asignadoA: String,
    val asignadoPor: String? = null,
    val maquinaId: Int? = null,
    val fechaLimite: Long,
    val prioridad: String,
    val estado: String,
    val fechaCreacion: Long,
    val fechaCumplimiento: Long? = null,
    val observaciones: String? = null,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)
