package com.biobox.biotech.data.local.dao

import androidx.room.*
import com.biobox.biotech.data.local.entity.MachineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MachineDao {
    @Query("SELECT * FROM machines ORDER BY updatedAt DESC")
    fun getAllMachines(): Flow<List<MachineEntity>>

    @Query("SELECT * FROM machines WHERE localId = :localId")
    fun getMachineByLocalId(localId: String): Flow<MachineEntity?>

    @Query("SELECT * FROM machines WHERE remoteId = :remoteId")
    suspend fun getMachineByRemoteId(remoteId: Int): MachineEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMachines(machines: List<MachineEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMachine(machine: MachineEntity)

    @Query("DELETE FROM machines WHERE localId = :localId")
    suspend fun deleteByLocalId(localId: String)

    @Query("DELETE FROM machines")
    suspend fun deleteAll()
    
    @Query("DELETE FROM machines WHERE syncStatus = 'SYNCED' AND remoteId NOT IN (:remoteIds)")
    suspend fun deleteStaleMachines(remoteIds: List<Int>)

    @Query("UPDATE machines SET remoteId = :remoteId, syncStatus = :status WHERE localId = :localId")
    suspend fun updateSyncMetadata(localId: String, remoteId: Int, status: com.biobox.biotech.core.common.SyncStatus)
}
