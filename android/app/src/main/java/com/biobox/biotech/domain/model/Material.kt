package com.biobox.biotech.domain.model

import com.biobox.biotech.core.common.SyncStatus

data class Material(
    val id: Int?,
    val codigo: String,
    val nombre: String,
    val cantidadRequerida: Int,
    val cantidadDisponible: Int,
    val estado: String,
    val descripcion: String? = null,
    val unidad: String? = null,
    val stockMin: Double = 0.0,
    val activo: Boolean = true,
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)
