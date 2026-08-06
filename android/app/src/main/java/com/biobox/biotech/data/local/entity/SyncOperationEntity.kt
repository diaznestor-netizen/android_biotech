package com.biobox.biotech.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sync_operations",
    indices = [
        Index(value = ["entityType", "entityLocalId", "operation"], unique = false) 
    ]
)
data class SyncOperationEntity(
    @PrimaryKey val id: String, // UUID
    val entityType: String,     // "PROJECT", "MACHINE", etc.
    val entityLocalId: String,
    val parentEntityLocalId: String? = null, // Para dependencias jerárquicas
    val operation: String,      // "CREATE", "UPDATE", "DELETE"
    val payloadJson: String,
    val status: SyncOperationStatus,
    val schemaVersion: Int = 1,  // Versionado del payload para evoluciones futuras
    val userId: String? = null,
    val organizationId: String? = null,
    val tenantId: String? = null,
    val idempotencyKey: String? = null,
    val conflictPayloadJson: String? = null,
    val retryCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastError: String? = null,
    val lastHttpStatusCode: Int? = null,
    val duration: Long? = null,    // Telemetría: duración de la última petición en ms
    val traceId: String? = null    // Trazabilidad extremo a extremo
)

enum class SyncOperationStatus {
    PENDING,
    IN_PROGRESS,
    SUCCESS,
    FAILED_RETRY,
    CONFLICT,
    ERROR
}
