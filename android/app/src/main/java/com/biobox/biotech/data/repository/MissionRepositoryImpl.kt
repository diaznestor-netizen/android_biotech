package com.biobox.biotech.data.repository

import com.biobox.biotech.data.local.dao.MissionDao
import com.biobox.biotech.data.local.dao.SyncOperationDao
import com.biobox.biotech.data.local.entity.SyncOperationEntity
import com.biobox.biotech.data.local.entity.SyncOperationStatus
import com.biobox.biotech.data.mapper.toDomain
import com.biobox.biotech.data.mapper.toEntity
import com.biobox.biotech.data.remote.api.MissionService
import com.biobox.biotech.data.remote.dto.CompleteMissionRequest
import com.biobox.biotech.domain.model.Mission
import com.biobox.biotech.domain.repository.MissionRepository
import com.biobox.biotech.domain.sync.GlobalSyncManager
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class MissionRepositoryImpl @Inject constructor(
    private val missionService: MissionService,
    private val missionDao: MissionDao,
    private val syncOperationDao: SyncOperationDao,
    private val globalSyncManager: GlobalSyncManager
) : MissionRepository {

    private val gson = Gson()

    override fun getMissions(): Flow<List<Mission>> {
        return missionDao.getAllMissions().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getMissionById(id: Int): Flow<Mission?> {
        return missionDao.getMissionById(id).map { it?.toDomain() }
    }

    override fun getCompletedMissions(): Flow<List<Mission>> {
        return missionDao.getCompletedMissions().map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun refreshMissions() {
        try {
            val response = missionService.getMissions()
            if (response.isSuccessful) {
                val missions = response.body().orEmpty().map { it.toEntity() }
                missionDao.insertMissions(missions)
            }
        } catch (_: Exception) { }
    }

    override suspend fun createMission(mission: Mission): Result<Mission> = runCatching {
        val entity = mission.toEntity()
        missionDao.insertMission(entity)
        syncOperationDao.insertOperation(SyncOperationEntity(
            id = UUID.randomUUID().toString(),
            entityType = "MISSION",
            entityLocalId = mission.id.toString(),
            operation = "CREATE",
            payloadJson = gson.toJson(mission),
            status = SyncOperationStatus.PENDING
        ))
        globalSyncManager.enqueueSync()
        mission
    }

    override suspend fun updateMission(mission: Mission): Result<Mission> = runCatching {
        val entity = mission.toEntity()
        missionDao.insertMission(entity)
        syncOperationDao.insertOperation(SyncOperationEntity(
            id = UUID.randomUUID().toString(),
            entityType = "MISSION",
            entityLocalId = mission.id.toString(),
            operation = "UPDATE",
            payloadJson = gson.toJson(mission),
            status = SyncOperationStatus.PENDING
        ))
        globalSyncManager.enqueueSync()
        mission
    }

    override suspend fun completeMission(id: Int, observaciones: String?): Result<Unit> = runCatching {
        val response = missionService.completeMission(id, CompleteMissionRequest(observaciones))
        if (!response.isSuccessful) throw Exception("Error al completar: ${response.code()}")
        refreshMissions()
    }

    override suspend fun approveMission(id: Int): Result<Unit> = runCatching {
        val response = missionService.approveMission(id)
        if (!response.isSuccessful) throw Exception("Error al aprobar: ${response.code()}")
        refreshMissions()
    }
}
