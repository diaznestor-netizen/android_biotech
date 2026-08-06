package com.biobox.biotech.data.local.dao

import androidx.room.*
import com.biobox.biotech.data.local.entity.MaterialEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MaterialDao {
    @Query("SELECT * FROM materials ORDER BY nombre ASC")
    fun getAllMaterials(): Flow<List<MaterialEntity>>

    @Query("SELECT * FROM materials WHERE nombre LIKE :query OR codigo LIKE :query")
    fun searchMaterials(query: String): Flow<List<MaterialEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterials(materials: List<MaterialEntity>)

    @Query("DELETE FROM materials")
    suspend fun deleteAll()
    
    @Query("DELETE FROM materials WHERE syncStatus = 'SYNCED' AND remoteId NOT IN (:remoteIds)")
    suspend fun deleteStaleMaterials(remoteIds: List<Int>)
}
