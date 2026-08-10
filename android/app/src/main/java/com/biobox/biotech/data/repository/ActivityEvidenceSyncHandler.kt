package com.biobox.biotech.data.repository

import com.biobox.biotech.data.local.entity.SyncOperationEntity
import com.biobox.biotech.data.remote.api.ActivityService
import com.biobox.biotech.domain.sync.SyncHandler
import com.biobox.biotech.domain.sync.SyncResult
import com.google.gson.Gson
import javax.inject.Inject

class ActivityEvidenceSyncHandler @Inject constructor(
    private val api: ActivityService
) : SyncHandler {
    private val gson = Gson()

    override suspend fun handle(operation: SyncOperationEntity): SyncResult {
        if (operation.operation != "DELETE") return SyncResult.Error("Operación no soportada")
        val payload = try {
            gson.fromJson(operation.payloadJson, ActivityEvidenceDeletePayload::class.java)
        } catch (e: Exception) {
            return SyncResult.Error("Error al deserializar evidencia: ${e.message}")
        }

        return try {
            val response = api.deleteEvidence(payload.activityId, payload.evidenceId)
            when {
                response.isSuccessful || response.code() == 404 -> SyncResult.Success
                else -> SyncResult.Retry("Error HTTP ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            SyncResult.Retry(e.message)
        }
    }
}
