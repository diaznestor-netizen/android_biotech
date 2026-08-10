package com.biobox.biotech.data.repository

import com.biobox.biotech.data.local.dao.EvidenceDao
import com.biobox.biotech.data.local.dao.InspectionDao
import com.biobox.biotech.data.local.entity.InspectionEntity
import com.biobox.biotech.data.local.entity.SyncOperationEntity
import com.biobox.biotech.core.common.SyncStatus
import com.biobox.biotech.data.mapper.toDomain
import com.biobox.biotech.data.mapper.toRequest
import com.biobox.biotech.data.remote.api.InspectionService
import com.biobox.biotech.domain.sync.SyncHandler
import com.biobox.biotech.domain.sync.SyncResult
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class InspectionSyncHandler @Inject constructor(
    private val api: InspectionService,
    private val dao: InspectionDao,
    private val evidenceDao: EvidenceDao
) : SyncHandler {
    override suspend fun handle(operation: SyncOperationEntity): SyncResult {
        val entity = dao.getInspectionById(operation.entityLocalId)
            ?: return SyncResult.Success

        return when (operation.operation) {
            "SUBMIT" -> performSubmit(entity)
            else -> SyncResult.Error("Operación no soportada: ${operation.operation}")
        }
    }

    private suspend fun performSubmit(entity: InspectionEntity): SyncResult {
        val inspection = entity.toDomain()
        return try {
            val revisionId = entity.remoteId ?: run {
                val response = api.submitInspection(inspection.toRequest())
                if (!response.isSuccessful) {
                    return SyncResult.Retry(response.errorBody()?.string() ?: "Error de red", response.code())
                }
                val id = response.body()?.id ?: return SyncResult.Retry("La API no devolvió el ID de revisión")
                dao.setRemoteId(entity.id, id)
                id
            }

            for (evidence in evidenceDao.getPendingByOwner("INSPECTION", entity.id)) {
                val file = File(evidence.localPath)
                if (!file.isFile || !file.canRead()) {
                    evidenceDao.updateSyncResult(evidence.id, SyncStatus.FAILED, null)
                    return SyncResult.Error("No se encuentra la evidencia local: ${file.name}")
                }
                val body = file.asRequestBody(evidence.mimeType.toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", file.name, body)
                val response = api.uploadEvidence(revisionId, part)
                if (!response.isSuccessful) {
                    evidenceDao.updateSyncResult(evidence.id, SyncStatus.FAILED, null)
                    return SyncResult.Retry(response.errorBody()?.string() ?: "Error subiendo evidencia", response.code())
                }
                evidenceDao.updateSyncResult(evidence.id, SyncStatus.SYNCED, response.body()?.url)
                deleteSyncedEvidence(file)
            }

            dao.deleteInspection(entity.id)
            SyncResult.Success
        } catch (e: Exception) {
            SyncResult.Retry(e.message)
        }
    }
}
