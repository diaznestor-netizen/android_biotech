package com.biobox.biotech.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.biobox.biotech.core.common.SyncStatus
import com.biobox.biotech.data.local.entity.EvidenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EvidenceDao {
    @Query("SELECT * FROM evidence WHERE ownerType = :ownerType AND ownerLocalId = :ownerLocalId ORDER BY createdAt")
    fun observeByOwner(ownerType: String, ownerLocalId: String): Flow<List<EvidenceEntity>>

    @Query("SELECT * FROM evidence WHERE syncStatus IN ('PENDING', 'FAILED') ORDER BY createdAt")
    suspend fun getPending(): List<EvidenceEntity>

    @Query("SELECT * FROM evidence WHERE ownerType = :ownerType AND ownerLocalId = :ownerLocalId AND syncStatus IN ('PENDING', 'FAILED') ORDER BY createdAt")
    suspend fun getPendingByOwner(ownerType: String, ownerLocalId: String): List<EvidenceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(evidence: EvidenceEntity)

    @Query("UPDATE evidence SET syncStatus = :status, remoteUrl = :remoteUrl, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateSyncResult(id: String, status: SyncStatus, remoteUrl: String?, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM evidence WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM evidence WHERE ownerType = :ownerType AND ownerLocalId = :ownerLocalId AND localPath = :localPath")
    suspend fun deleteByOwnerAndPath(ownerType: String, ownerLocalId: String, localPath: String)
}
