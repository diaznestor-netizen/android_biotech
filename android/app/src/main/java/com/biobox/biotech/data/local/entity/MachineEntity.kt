package com.biobox.biotech.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.biobox.biotech.core.common.SyncStatus

@Entity(tableName = "machines")
data class MachineEntity(
    @PrimaryKey val localId: String,
    val remoteId: Int?,
    val codigo: String,
    val nombre: String,
    val area: String,
    val estado: String,
    val porcentajeAvance: Int,
    val imagenUrl: String?,
    val responsable: String?,
    val ultimaRevision: String?,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val updatedAt: Long = System.currentTimeMillis()
)
