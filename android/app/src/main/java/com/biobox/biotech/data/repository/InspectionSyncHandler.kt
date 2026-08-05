package com.biobox.biotech.data.repository

import com.biobox.biotech.data.local.dao.InspectionDao
import com.biobox.biotech.data.local.entity.SyncOperationEntity
import com.biobox.biotech.data.mapper.toDomain
import com.biobox.biotech.data.mapper.toRequest
import com.biobox.biotech.data.remote.api.InspectionService
import com.biobox.biotech.domain.model.Inspection
import com.biobox.biotech.domain.sync.SyncHandler
import com.biobox.biotech.domain.sync.SyncResult
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class InspectionSyncHandler @Inject constructor(
    private val api: InspectionService,
    private val dao: InspectionDao
) : SyncHandler {
    private val gson = Gson()

    override suspend fun handle(operation: SyncOperationEntity): SyncResult {
        val inspection = dao.getInspectionById(operation.entityLocalId)?.toDomain()
            ?: return SyncResult.Success

        return when (operation.operation) {
            "SUBMIT" -> performSubmit(operation, inspection)
            else -> SyncResult.Error("Operación no soportada: ${operation.operation}")
        }
    }

    private suspend fun performSubmit(operation: SyncOperationEntity, inspection: Inspection): SyncResult {
        return try {
            val response = api.submitInspection(inspection.toRequest())
            if (response.isSuccessful) {
                dao.deleteInspection(operation.entityLocalId)
                SyncResult.Success
            } else {
                SyncResult.Retry(response.errorBody()?.string() ?: "Error de red", response.code())
            }
        } catch (e: Exception) {
            SyncResult.Retry(e.message)
        }
    }
}
