package com.biobox.biotech.data.repository

import com.biobox.biotech.core.common.SyncStatus
import com.biobox.biotech.data.local.dao.ActivityDao
import com.biobox.biotech.data.local.dao.EvidenceDao
import com.biobox.biotech.data.local.entity.SyncOperationEntity
import com.biobox.biotech.data.mapper.toDomain
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
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ActivitySyncHandler @Inject constructor(
    private val api: ActivityService,
    private val dao: ActivityDao,
    private val evidenceDao: EvidenceDao
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
        return try {
            val request = ActivityRequest(
                titulo = activity.titulo,
                descripcion = activity.descripcion,
                responsable = activity.responsable,
                maquinaId = activity.maquinaId,
                tiempoEmpleado = activity.tiempoEmpleado.toString(),
                fecha = activity.fecha.toApiDate(),
                comentarios = activity.comentarios
            )
            val response = api.createActivity(request)
            if (response.isSuccessful) {
                val created = response.body() ?: return SyncResult.Retry("La API no devolvió la actividad")
                val remoteId = created.id ?: return SyncResult.Retry("La API no devolvió el ID de actividad")
                for (evidence in evidenceDao.getPendingByOwner("ACTIVITY", operation.entityLocalId)) {
                    val file = File(evidence.localPath)
                    if (!file.isFile || !file.canRead()) {
                        evidenceDao.updateSyncResult(evidence.id, SyncStatus.FAILED, null)
                        return SyncResult.Error("No se encuentra la evidencia local: ${file.name}")
                    }
                    val part = MultipartBody.Part.createFormData(
                        "file",
                        file.name,
                        file.asRequestBody(evidence.mimeType.toMediaTypeOrNull())
                    )
                    val upload = api.uploadEvidence(remoteId, part)
                    if (!upload.isSuccessful) {
                        evidenceDao.updateSyncResult(evidence.id, SyncStatus.FAILED, null)
                        return SyncResult.Retry(upload.errorBody()?.string() ?: "Error subiendo evidencia", upload.code())
                    }
                    evidenceDao.updateSyncResult(evidence.id, SyncStatus.SYNCED, upload.body()?.url)
                    deleteSyncedEvidence(file)
                }
                dao.deleteActivity(activity.id)
                dao.insertActivity(created.toEntity())
                SyncResult.Success
            } else {
                SyncResult.Retry("Error HTTP ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            SyncResult.Retry(e.message)
        }
    }

    private suspend fun performUpdate(operation: SyncOperationEntity, activity: Activity): SyncResult {
        return try {
            val request = ActivityRequest(
                titulo = activity.titulo,
                descripcion = activity.descripcion,
                responsable = activity.responsable,
                maquinaId = activity.maquinaId,
                tiempoEmpleado = activity.tiempoEmpleado.toString(),
                fecha = activity.fecha.toApiDate(),
                comentarios = activity.comentarios
            )
            val response = api.updateActivity(activity.id, request)
            if (response.isSuccessful) {
                response.body()?.let { dao.insertActivity(it.toEntity()) }
                for (evidence in evidenceDao.getPendingByOwner("ACTIVITY", operation.entityLocalId)) {
                    val file = File(evidence.localPath)
                    if (!file.isFile || !file.canRead()) {
                        evidenceDao.updateSyncResult(evidence.id, SyncStatus.FAILED, null)
                        return SyncResult.Error("No se encuentra la evidencia local: ${file.name}")
                    }
                    val part = MultipartBody.Part.createFormData(
                        "file",
                        file.name,
                        file.asRequestBody(evidence.mimeType.toMediaTypeOrNull())
                    )
                    val upload = api.uploadEvidence(activity.id, part)
                    if (!upload.isSuccessful) {
                        evidenceDao.updateSyncResult(evidence.id, SyncStatus.FAILED, null)
                        return SyncResult.Retry(upload.errorBody()?.string() ?: "Error subiendo evidencia", upload.code())
                    }
                    evidenceDao.updateSyncResult(evidence.id, SyncStatus.SYNCED, upload.body()?.url)
                    deleteSyncedEvidence(file)
                }
                SyncResult.Success
            } else {
                SyncResult.Retry("Error HTTP ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            SyncResult.Retry(e.message)
        }
    }
}

private fun Long.toApiDate(): String = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ROOT).format(Date(this))
