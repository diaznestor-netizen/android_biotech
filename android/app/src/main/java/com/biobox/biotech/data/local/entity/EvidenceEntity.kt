package com.biobox.biotech.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.biobox.biotech.core.common.SyncStatus

@Entity(
    tableName = "evidence",
    indices = [Index(value = ["ownerType", "ownerLocalId"]), Index(value = ["syncStatus"])]
)
data class EvidenceEntity(
    @PrimaryKey val id: String,
    val ownerType: String,
    val ownerLocalId: String,
    val localPath: String,
    val mimeType: String = "image/jpeg",
    val remoteUrl: String? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
