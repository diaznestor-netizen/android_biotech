package com.biobox.biotech.data.local.dao

import androidx.room.*
import com.biobox.biotech.data.local.entity.SyncOperationEntity
import com.biobox.biotech.data.local.entity.SyncOperationStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncOperationDao {
    @Query("SELECT * FROM sync_operations WHERE status IN ('PENDING', 'FAILED_RETRY') ORDER BY createdAt ASC")
    fun getPendingOperations(): Flow<List<SyncOperationEntity>>

    @Query("""
        SELECT * FROM sync_operations 
        WHERE entityType = :entityType 
          AND entityLocalId = :localId 
          AND status IN ('PENDING', 'IN_PROGRESS', 'FAILED_RETRY')
        LIMIT 1
    """)
    suspend fun findActiveOperation(entityType: String, localId: String): SyncOperationEntity?

    @Query("SELECT * FROM sync_operations WHERE status IN ('PENDING', 'FAILED_RETRY') ORDER BY createdAt ASC")
    suspend fun getPendingOperationsOnce(): List<SyncOperationEntity>

    @Query("""
        UPDATE sync_operations 
        SET status = :newStatus, 
            updatedAt = :updatedAt 
        WHERE id = :id 
          AND status = :expectedStatus
    """)
    suspend fun compareAndSetStatus(
        id: String,
        expectedStatus: SyncOperationStatus,
        newStatus: SyncOperationStatus,
        updatedAt: Long
    ): Int

    @Query("SELECT COUNT(*) FROM sync_operations WHERE status IN ('PENDING', 'FAILED_RETRY')")
    fun getPendingCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperation(operation: SyncOperationEntity)

    @Update
    suspend fun updateOperation(operation: SyncOperationEntity)

    @Delete
    suspend fun deleteOperation(operation: SyncOperationEntity)

    @Query("DELETE FROM sync_operations WHERE status = 'SUCCESS'")
    suspend fun deleteSyncedOperations(): Int

    @Query("DELETE FROM sync_operations")
    suspend fun deleteAll(): Int

    @Query("SELECT * FROM sync_operations WHERE entityLocalId = :localId AND entityType = :type")
    suspend fun getOperationsByEntity(localId: String, type: String): List<SyncOperationEntity>

    @Query("DELETE FROM sync_operations WHERE entityType = :entityType AND entityLocalId = :localId")
    suspend fun deleteByEntity(entityType: String, localId: String): Int

    @Query("SELECT COUNT(*) FROM sync_operations WHERE entityLocalId = :parentLocalId AND status != 'SUCCESS'")
    suspend fun getActiveOperationsCountForParent(parentLocalId: String): Int
}
