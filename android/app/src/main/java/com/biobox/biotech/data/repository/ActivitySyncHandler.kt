package com.biobox.biotech.data.repository

import com.biobox.biotech.core.common.SyncStatus
import com.biobox.biotech.data.local.dao.ActivityDao
import com.biobox.biotech.data.local.dao.EvidenceDao
import com.biobox.biotech.data.local.dao.SyncOperationDao
import com.biobox.biotech.data.local.entity.EvidenceEntity
import com.biobox.biotech.data.local.entity.SyncOperationEntity
import com.biobox.biotech.data.local.entity.SyncOperationStatus
import com.biobox.biotech.data.mapper.toEntity
import com.biobox.biotech.data.remote.api.ActivityService
import com.biobox.biotech.data.remote.dto.ActivityRequest
import com.biobox.biotech.domain.model.Activity
import com.biobox.biotech.domain.sync.SyncHandler
import com.biobox.biotech.domain.sync.SyncResult
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.UUID
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivitySyncHandler @Inject constructor(
    private val api: ActivityService,
    private val dao: ActivityDao,
    private val evidenceDao: EvidenceDao,
    private val syncOperationDao: SyncOperationDao
) : SyncHandler {
    private val gson = Gson()

    override suspend fun handle(operation: SyncOperationEntity): SyncResult {
        val activity = try {
            gson.fromJson(operation.payloadJson, Activity::class.java)
        } catch (e: Exception) {
            return SyncResult.Error("Error al deserializar actividad: ${e.message}")
        }

        return when (operation.operation) {
            "CREATE" -> performCreate(operation, activity)
            "UPDATE" -> performUpdate(operation, activity)
            else -> SyncResult.Error("Operación no soportada")
        }
    }

    private suspend fun performCreate(operation: SyncOperationEntity, activity: Activity): SyncResult {
        val response = try {
            api.createActivity(activity.toRequest())
        } catch (e: Exception) {
            null
        } ?: return SyncResult.Retry("Sin conexión al crear la actividad")

        if (response.code() in 400..499) {
            // Error de validación/permanente: reintentar nunca lo resolverá
            return SyncResult.Error("Error permanente HTTP ${response.code()} al crear la actividad", response.code())
        }
        if (!response.isSuccessful) {
            return SyncResult.Retry("Error HTTP ${response.code()}", response.code())
        }
        val created = response.body() ?: return SyncResult.Retry("La API no devolvió la actividad")
        val remoteId = created.id ?: return SyncResult.Retry("La API no devolvió el ID de actividad")

        // Punto de no retorno: la actividad YA existe en el servidor. Desde aquí la
        // operación jamás devuelve Retry, para que un fallo de evidencia no recree
        // (duplique) la actividad remota.
        dao.deleteActivity(activity.id)
        dao.insertActivity(created.toEntity())

        for (evidence in evidenceDao.getPendingByOwner("ACTIVITY", operation.entityLocalId)) {
            uploadEvidenceOrEnqueue(remoteId, evidence, operation.entityLocalId)
        }
        return SyncResult.Success
    }

    private suspend fun performUpdate(operation: SyncOperationEntity, activity: Activity): SyncResult {
        val response = try {
            api.updateActivity(activity.id, activity.toRequest())
        } catch (e: Exception) {
            null
        } ?: return SyncResult.Retry("Sin conexión al actualizar la actividad")

        return when {
            response.isSuccessful -> {
                response.body()?.let { dao.insertActivity(it.toEntity()) }
                // Un fallo de evidencia no invalida el UPDATE ya aplicado en el servidor
                for (evidence in evidenceDao.getPendingByOwner("ACTIVITY", operation.entityLocalId)) {
                    uploadEvidenceOrEnqueue(activity.id, evidence, operation.entityLocalId)
                }
                SyncResult.Success
            }
            response.code() in 400..499 ->
                SyncResult.Error("Error permanente HTTP ${response.code()} al actualizar la actividad", response.code())
            else -> SyncResult.Retry("Error HTTP ${response.code()}", response.code())
        }
    }

    /**
     * Sube una evidencia local. Si la red o el servidor fallan de forma transitoria,
     * encola una operación ACTIVITY_EVIDENCE/UPLOAD independiente en lugar de
     * reintentar toda la operación de actividad (que duplicaría el registro remoto).
     */
    private suspend fun uploadEvidenceOrEnqueue(activityRemoteId: Int, evidence: EvidenceEntity, parentLocalId: String) {
        val file = File(evidence.localPath)
        if (!file.isFile || !file.canRead()) {
            evidenceDao.updateSyncResult(evidence.id, SyncStatus.FAILED, null)
            return
        }
        val part = MultipartBody.Part.createFormData(
            "file",
            file.name,
            file.asRequestBody(evidence.mimeType.toMediaTypeOrNull())
        )
        val upload = try {
            api.uploadEvidence(activityRemoteId, part)
        } catch (e: Exception) {
            null
        }
        when {
            upload == null || upload.code() >= 500 -> enqueueEvidenceUpload(activityRemoteId, evidence, parentLocalId)
            upload.isSuccessful -> {
                evidenceDao.updateSyncResult(evidence.id, SyncStatus.SYNCED, upload.body()?.url)
                deleteSyncedEvidence(file)
            }
            else -> evidenceDao.updateSyncResult(evidence.id, SyncStatus.FAILED, null) // 4xx permanente
        }
    }

    private suspend fun enqueueEvidenceUpload(activityRemoteId: Int, evidence: EvidenceEntity, parentLocalId: String) {
        syncOperationDao.insertOperation(SyncOperationEntity(
            id = UUID.randomUUID().toString(),
            entityType = "ACTIVITY_EVIDENCE",
            entityLocalId = evidence.id,
            parentEntityLocalId = parentLocalId,
            operation = "UPLOAD",
            payloadJson = gson.toJson(ActivityEvidenceUploadPayload(activityRemoteId, evidence.id, evidence.localPath, evidence.mimeType)),
            status = SyncOperationStatus.PENDING
        ))
    }

    private fun Activity.toRequest() = ActivityRequest(
        titulo = titulo,
        descripcion = descripcion,
        responsable = responsable,
        maquinaId = maquinaId,
        tiempoEmpleado = tiempoEmpleado.toString(),
        fecha = fecha.toApiDate(),
        comentarios = comentarios
    )
}

private fun Long.toApiDate(): String = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ROOT).format(Date(this))
