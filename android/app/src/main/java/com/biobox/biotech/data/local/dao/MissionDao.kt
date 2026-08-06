package com.biobox.biotech.data.local.dao

import androidx.room.*
import com.biobox.biotech.data.local.entity.MissionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MissionDao {
    @Query("SELECT * FROM missions ORDER BY fechaLimite ASC")
    fun getAllMissions(): Flow<List<MissionEntity>>

    @Query("SELECT * FROM missions WHERE id = :id")
    fun getMissionById(id: Int): Flow<MissionEntity?>

    @Query("SELECT * FROM missions WHERE estado IN ('COMPLETADA', 'APROBADA') ORDER BY fechaCumplimiento DESC")
    fun getCompletedMissions(): Flow<List<MissionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMissions(missions: List<MissionEntity>)

    @Query("DELETE FROM missions")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMission(mission: MissionEntity)

    @Query("DELETE FROM missions WHERE id = :id")
    suspend fun deleteMission(id: Int)
}
