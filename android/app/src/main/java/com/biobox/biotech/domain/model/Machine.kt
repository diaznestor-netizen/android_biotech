package com.biobox.biotech.domain.model

import com.biobox.biotech.core.common.SyncStatus

data class Machine(
    val id: Int,
    val codigo: String,
    val nombre: String,
    val area: String,
    val estado: MachineStatus,
    val porcentajeAvance: Int,
    val imagenUrl: String? = null,
    val responsable: String? = null,
    val ultimaRevision: String? = null,
    val componentes: List<Component> = emptyList(),
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)

data class Component(
    val id: Int,
    val nombre: String,
    val estado: String,
    val materiales: List<Material> = emptyList()
)

enum class MachineStatus {
    COMPLETA, CASI_COMPLETA, INCOMPLETA, NO_OPERATIVA;

    companion object {
        fun fromPercentage(percentage: Int, hasCriticalMissing: Boolean): MachineStatus {
            if (hasCriticalMissing) return NO_OPERATIVA
            return when {
                percentage >= 100 -> COMPLETA
                percentage >= 80 -> CASI_COMPLETA
                percentage >= 50 -> INCOMPLETA
                else -> NO_OPERATIVA
            }
        }
    }
}
