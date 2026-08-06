package com.biobox.biotech.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.biobox.biotech.core.common.SyncStatus

@Entity(
    tableName = "projects",
    indices = [
        Index(value = ["codigo", "organizationId", "tenantId"], unique = true)
    ]
)
data class ProjectEntity(
    @PrimaryKey val localId: String,
    val remoteId: Int?,
    val codigo: String,
    val nombre: String,
    val descripcion: String?,
    val cliente: String?,
    val responsableId: Int?,
    val responsableNombre: String?,
    val usuarioCreadorId: Int?,
    val estado: String,
    val prioridad: String,
    val fechaInicio: Long?,
    val fechaFinEstimada: Long?,
    val fechaFinReal: Long?,
    val porcentajeAvance: Int,
    val observaciones: String?,
    val version: Int = 0,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val conflictPayloadJson: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null,
    val organizationId: String? = null,
    val tenantId: String? = null
)
