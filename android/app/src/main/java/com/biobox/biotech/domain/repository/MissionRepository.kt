package com.biobox.biotech.domain.repository

import com.biobox.biotech.domain.model.Mission
import kotlinx.coroutines.flow.Flow

interface MissionRepository {
    fun getMissions(): Flow<List<Mission>>
    fun getMissionById(id: Int): Flow<Mission?>
    fun getCompletedMissions(): Flow<List<Mission>>
    suspend fun refreshMissions()
    suspend fun createMission(mission: Mission): Result<Mission>
    suspend fun updateMission(mission: Mission): Result<Mission>
    suspend fun completeMission(id: Int, observaciones: String?): Result<Unit>
    suspend fun approveMission(id: Int): Result<Unit>
}
