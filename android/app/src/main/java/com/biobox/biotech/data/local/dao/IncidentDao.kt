package com.biobox.biotech.data.local.dao

import androidx.room.*
import com.biobox.biotech.data.local.entity.IncidentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidentDao {
    @Query("SELECT * FROM incidents ORDER BY fechaReporte DESC")
    fun getAllIncidents(): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM incidents WHERE id = :id")
    fun getIncidentById(id: Int): Flow<IncidentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncidents(incidents: List<IncidentEntity>)

    @Query("DELETE FROM incidents")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: IncidentEntity)

    @Query("DELETE FROM incidents WHERE id = :id")
    suspend fun deleteIncident(id: Int)
}
