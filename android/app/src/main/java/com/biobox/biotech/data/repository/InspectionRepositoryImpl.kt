package com.biobox.biotech.data.repository

import com.biobox.biotech.data.local.dao.InspectionDao
import com.biobox.biotech.data.local.dao.EvidenceDao
import com.biobox.biotech.data.local.entity.EvidenceEntity
import com.biobox.biotech.data.local.dao.SyncOperationDao
import com.biobox.biotech.data.local.entity.SyncOperationEntity
import com.biobox.biotech.data.local.entity.SyncOperationStatus
import com.biobox.biotech.data.mapper.toDomain
import com.biobox.biotech.data.mapper.toEntity
import com.biobox.biotech.data.mapper.toRequest
import com.biobox.biotech.data.remote.api.InspectionService
import com.biobox.biotech.domain.model.Inspection
import com.biobox.biotech.domain.model.InspectionSummary
import com.biobox.biotech.domain.repository.InspectionRepository
import com.biobox.biotech.domain.sync.GlobalSyncManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class InspectionRepositoryImpl @Inject constructor(
    private val api: InspectionService,
    private val dao: InspectionDao,
    private val evidenceDao: EvidenceDao,
    private val syncOperationDao: SyncOperationDao,
    private val globalSyncManager: GlobalSyncManager
) : InspectionRepository {
    override suspend fun getInspections(): Result<List<InspectionSummary>> {
        return try {
            val response = api.getInspections()
            if (response.isSuccessful) {
                Result.success(response.body().orEmpty().map {
                    InspectionSummary(
                        id = it.id,
                        machineId = it.machineId,
                        machineCode = it.machineCode,
                        machineName = it.machineName,
                        auditor = it.auditor,
                        date = it.date,
                        status = it.status,
                        progress = it.progress,
                        notes = it.notes
                    )
                })
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error cargando revisiones"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun submitInspection(inspection: Inspection): Result<Unit> {
        return try {
            val response = api.submitInspection(inspection.toRequest())
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.errorBody()?.string() ?: "Error de red"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun savePendingInspection(inspection: Inspection) {
        dao.insertInspection(inspection.toEntity())
        inspection.evidencePaths.distinct().forEach { path ->
            evidenceDao.insert(EvidenceEntity(
                id = UUID.nameUUIDFromBytes("${inspection.id}:$path".toByteArray()).toString(),
                ownerType = "INSPECTION",
                ownerLocalId = inspection.id,
                localPath = path,
                mimeType = java.net.URLConnection.guessContentTypeFromName(path) ?: "image/jpeg"
            ))
        }
        syncOperationDao.insertOperation(SyncOperationEntity(
            id = UUID.randomUUID().toString(),
            entityType = "INSPECTION",
            entityLocalId = inspection.id,
            operation = "SUBMIT",
            payloadJson = "", // Payload is in the specific table
            status = SyncOperationStatus.PENDING
        ))
        globalSyncManager.enqueueSync()
    }

    override fun getPendingInspections(): Flow<List<Inspection>> {
        return dao.getPendingInspections().map { entities ->
            entities.map { it.toDomain() }
        }
    }

}

