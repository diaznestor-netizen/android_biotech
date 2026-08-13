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
import java.io.File
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
        activity.evidencias
            .filter { !it.startsWith("/api/v1/evidence/") }
            .distinct()
            .forEach { path ->
                evidenceDao.insert(EvidenceEntity(UUID.randomUUID().toString(), "ACTIVITY", activity.id.toString(), path))
            }
        
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

    override suspend fun deleteActivityEvidence(activityId: Int, evidenceUrl: String): Result<Unit> = runCatching {
        // 1) Reflejar el borrado en la entidad local siempre, remota o no
        activityDao.getActivityByIdOnce(activityId)?.toDomain()?.let { current ->
            activityDao.insertActivity(current.copy(evidencias = current.evidencias.filterNot { it == evidenceUrl }).toEntity())
        }

        val evidenceId = evidenceIdFromUrl(evidenceUrl)
        if (evidenceId == null) {
            // Evidencia local aún no sincronizada: quitar de la cola y borrar el archivo
            evidenceDao.deleteByOwnerAndPath("ACTIVITY", activityId.toString(), evidenceUrl)
            runCatching { File(evidenceUrl).delete() }
            return@runCatching
        }

        // 2) Evidencia remota: intentar borrar en el servidor
        val response = try {
            activityService.deleteEvidence(activityId, evidenceId)
        } catch (_: Exception) {
            null
        }

        when {
            // Sin red o fallo transitorio del servidor: encolar para el próximo sync
            response == null || response.code() >= 500 -> enqueueEvidenceDelete(activityId, evidenceId)
            response.isSuccessful || response.code() == 404 -> refreshActivity(activityId)
            // 4xx restante (permisos, validación): permanente, no tiene sentido encolar
            else -> throw Exception("No se pudo eliminar la foto (HTTP ${response.code()})")
        }
    }

    private suspend fun enqueueEvidenceDelete(activityId: Int, evidenceId: Int) {
        syncOperationDao.insertOperation(SyncOperationEntity(
            id = UUID.randomUUID().toString(),
            entityType = "ACTIVITY_EVIDENCE",
            entityLocalId = evidenceId.toString(),
            parentEntityLocalId = activityId.toString(),
            operation = "DELETE",
            payloadJson = gson.toJson(ActivityEvidenceDeletePayload(activityId, evidenceId)),
            status = SyncOperationStatus.PENDING
        ))
        globalSyncManager.enqueueSync()
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

    private suspend fun refreshActivity(id: Int) {
        try {
            val response = activityService.getActivityById(id)
            if (response.isSuccessful) {
                response.body()?.let { activityDao.insertActivity(it.toEntity()) }
            }
        } catch (_: Exception) { }
    }
}

data class ActivityEvidenceDeletePayload(
    val activityId: Int,
    val evidenceId: Int
)

fun evidenceIdFromUrl(url: String): Int? {
    return Regex("/evidence/(\\d+)").find(url)?.groupValues?.getOrNull(1)?.toIntOrNull()
}
