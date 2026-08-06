package com.biobox.biotech.domain.model

import java.util.UUID

data class Inspection(
    val id: String = UUID.randomUUID().toString(),
    val machineId: Int,
    val items: List<InspectionItem>,
    val observaciones: String? = null,
    val evidencePaths: List<String> = emptyList(),
    val status: SyncStatus = SyncStatus.PENDING,
    val timestamp: Long = System.currentTimeMillis()
)

data class InspectionItem(
    val materialId: Int,
    val cantidadEncontrada: Int
)

enum class SyncStatus {
    PENDING, SYNCED, ERROR
}

data class InspectionSummary(
    val id: Int,
    val machineId: Int,
    val machineCode: String,
    val machineName: String,
    val auditor: String,
    val date: String,
    val status: String,
    val progress: Double,
    val notes: String? = null
)
