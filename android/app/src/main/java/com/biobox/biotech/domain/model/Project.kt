package com.biobox.biotech.domain.model

import com.biobox.biotech.core.common.SyncStatus

data class Project(
    val id: Int?,
    val localId: String,
    val codigo: String,
    val nombre: String,
    val descripcion: String? = null,
    val cliente: String? = null,
    val responsableId: Int? = null,
    val responsableNombre: String? = null,
    val usuarioCreadorId: Int? = null,
    val estado: ProjectStatus = ProjectStatus.PLANEADO,
    val prioridad: ProjectPriority = ProjectPriority.MEDIA,
    val fechaInicio: Long? = null,
    val fechaFinEstimada: Long? = null,
    val fechaFinReal: Long? = null,
    val porcentajeAvance: Int = 0,
    val observaciones: String? = null,
    val version: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
    val conflictPayloadJson: String? = null,
    val organizationId: String? = null,
    val tenantId: String? = null
)
