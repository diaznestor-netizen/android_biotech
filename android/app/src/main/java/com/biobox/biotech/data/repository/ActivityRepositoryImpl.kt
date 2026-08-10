package com.biobox.biotech.data.repository

import com.biobox.biotech.data.local.dao.ActivityDao
import com.biobox.biotech.data.local.dao.EvidenceDao
import com.biobox.biotech.data.local.dao.SyncOperationDao
import com.biobox.biotech.data.local.entity.EvidenceEntity
import com.biobox.biotech.data.local.entity.SyncOperationEntity
import com.biobox.biotech.data.local.entity.SyncOperationStatus
import com.biobox.biotech.data.mapper.toDomain
import com.biobox.biotech.data.mapper.toEntity
import com.biobox.biotech.data.remote.api.ActivityService
import com.biobox.biotech.data.remote.dto.ActivityRequest
import com.biobox.biotech.data.remote.dto.ApproveRejectRequest
import com.biobox.biotech.domain.model.Activity
import com.biobox.biotech.domain.repository.ActivityRepository
import com.biobox.biotech.domain.sync.GlobalSyncManager
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class ActivityRepositoryImpl @Inject constructor(
    private val activityService: ActivityService,
    private val activityDao: ActivityDao,
    private val evidenceDao: EvidenceDao,
    private val syncOperationDao: SyncOperationDao,
    private val globalSyncManager: GlobalSyncManager
) : ActivityRepository {

    private val gson = Gson()

    override fun getActivities(): Flow<List<Activity>> {
        return activityDao.getAllActivities().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getActivityById(id: Int): Flow<Activity?> {
        return activityDao.getActivityById(id).map { it?.toDomain() }
    }

    override suspend fun refreshActivities() {
        try {
            val response = activityService.getActivities()
            if (response.isSuccessful) {
                val activities = response.body().orEmpty().map { it.toEntity() }
                activityDao.insertActivities(activities)
            }
        } catch (_: Exception) { }
    }

    override suspend fun createActivity(activity: Activity): Result<Activity> = runCatching {
        // En lugar de llamar a la API directamente, guardamos localmente y encolamos
        val entity = activity.toEntity()
        activityDao.insertActivity(entity)
        activity.evidencias.distinct().forEach { path ->
            evidenceDao.insert(EvidenceEntity(UUID.randomUUID().toString(), "ACTIVITY", activity.id.toString(), path))
        }
        
        syncOperationDao.insertOperation(SyncOperationEntity(
            id = UUID.randomUUID().toString(),
            entityType = "ACTIVITY",
            entityLocalId = activity.id.toString(),
            operation = "CREATE",
            payloadJson = gson.toJson(activity),
            status = SyncOperationStatus.PENDING
        ))
        globalSyncManager.enqueueSync()
        activity
    }

    override suspend fun updateActivity(activity: Activity): Result<Activity> = runCatching {
        val entity = activity.toEntity()
        activityDao.insertActivity(entity)
        
        syncOperationDao.insertOperation(SyncOperationEntity(
            id = UUID.randomUUID().toString(),
            entityType = "ACTIVITY",
            entityLocalId = activity.id.toString(),
            operation = "UPDATE",
            payloadJson = gson.toJson(activity),
            status = SyncOperationStatus.PENDING
        ))
        globalSyncManager.enqueueSync()
        activity
    }

    override suspend fun approveActivity(id: Int): Result<Unit> = runCatching {
        val response = activityService.approveActivity(id)
        if (!response.isSuccessful) throw Exception("Error al aprobar: ${response.code()}")
        refreshActivities()
    }

    override suspend fun rejectActivity(id: Int, motivo: String): Result<Unit> = runCatching {
        val response = activityService.rejectActivity(id, ApproveRejectRequest("rechazar", motivo))
        if (!response.isSuccessful) throw Exception("Error al rechazar: ${response.code()}")
        refreshActivities()
    }
}
