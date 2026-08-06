package com.biobox.biotech.data.local.dao

import androidx.room.*
import com.biobox.biotech.data.local.entity.InspectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InspectionDao {
    @Query("SELECT * FROM pending_inspections ORDER BY timestamp ASC")
    fun getPendingInspections(): Flow<List<InspectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspection(inspection: InspectionEntity)

    @Query("SELECT * FROM pending_inspections WHERE id = :id")
    suspend fun getInspectionById(id: String): InspectionEntity?

    @Query("DELETE FROM pending_inspections WHERE id = :id")
    suspend fun deleteInspection(id: String)
}
