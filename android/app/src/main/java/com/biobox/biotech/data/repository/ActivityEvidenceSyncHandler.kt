package com.biobox.biotech.data.repository

import com.biobox.biotech.core.common.SyncStatus
import com.biobox.biotech.data.local.dao.EvidenceDao
import com.biobox.biotech.data.local.entity.SyncOperationEntity
import com.biobox.biotech.data.remote.api.ActivityService
import com.biobox.biotech.domain.sync.SyncHandler
import com.biobox.biotech.domain.sync.SyncResult
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class ActivityEvidenceSyncHandler @Inject constructor(
    private val api: ActivityService,
    private val evidenceDao: EvidenceDao
) : SyncHandler {
    private val gson = Gson()

    override suspend fun handle(operation: SyncOperationEntity): SyncResult {
        return when (operation.operation) {
            "DELETE" -> handleDelete(operation)
            "UPLOAD" -> handleUpload(operation)
            else -> SyncResult.Error("Operación no soportada: ${operation.operation}")
        }
    }

    private suspend fun handleDelete(operation: SyncOperationEntity): SyncResult {
        val payload = try {
            gson.fromJson(operation.payloadJson, ActivityEvidenceDeletePayload::class.java)
        } catch (e: Exception) {
            return SyncResult.Error("Error al deserializar evidencia: ${e.message}")
        }

        return try {
            val response = api.deleteEvidence(payload.activityId, payload.evidenceId)
            when {
                response.isSuccessful || response.code() == 404 -> SyncResult.Success
                // 4xx (distinto de 404) es permanente: reintentar nunca lo resolverá
                response.code() in 400..499 -> SyncResult.Error("Error permanente HTTP ${response.code()} al eliminar evidencia", response.code())
                else -> SyncResult.Retry("Error HTTP ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            SyncResult.Retry(e.message)
        }
    }

    private suspend fun handleUpload(operation: SyncOperationEntity): SyncResult {
        val payload = try {
            gson.fromJson(operation.payloadJson, ActivityEvidenceUploadPayload::class.java)
        } catch (e: Exception) {
            return SyncResult.Error("Error al deserializar evidencia: ${e.message}")
        }

        val file = File(payload.localPath)
        if (!file.isFile || !file.canRead()) {
            evidenceDao.updateSyncResult(payload.evidenceLocalId, SyncStatus.FAILED, null)
            return SyncResult.Error("No se encuentra la evidencia local: ${file.name}")
        }
        val part = MultipartBody.Part.createFormData(
            "file",
            file.name,
            file.asRequestBody(payload.mimeType.toMediaTypeOrNull())
        )
        val upload = try {
            api.uploadEvidence(payload.activityId, part)
        } catch (e: Exception) {
            null
        }
        return when {
            upload == null -> SyncResult.Retry("Sin conexión al subir evidencia")
            upload.isSuccessful -> {
                evidenceDao.updateSyncResult(payload.evidenceLocalId, SyncStatus.SYNCED, upload.body()?.url)
                deleteSyncedEvidence(file)
                SyncResult.Success
            }
            upload.code() in 400..499 -> {
                evidenceDao.updateSyncResult(payload.evidenceLocalId, SyncStatus.FAILED, null)
                SyncResult.Error("Error permanente HTTP ${upload.code()} al subir evidencia", upload.code())
            }
            else -> SyncResult.Retry("Error HTTP ${upload.code()}", upload.code())
        }
    }
}

data class ActivityEvidenceUploadPayload(
    val activityId: Int,
    val evidenceLocalId: String,
    val localPath: String,
    val mimeType: String
)
