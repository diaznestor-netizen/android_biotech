package com.biobox.biotech.data.repository

import com.biobox.biotech.data.local.dao.IncidentDao
import com.biobox.biotech.data.local.entity.SyncOperationEntity
import com.biobox.biotech.data.mapper.toDomain
import com.biobox.biotech.data.mapper.toEntity
import com.biobox.biotech.data.remote.api.IncidentService
import com.biobox.biotech.data.remote.dto.IncidentRequest
import com.biobox.biotech.domain.model.Incident
import com.biobox.biotech.domain.sync.SyncHandler
import com.biobox.biotech.domain.sync.SyncResult
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class IncidentSyncHandler @Inject constructor(
    private val api: IncidentService,
    private val dao: IncidentDao
) : SyncHandler {
    private val gson = Gson()

    override suspend fun handle(operation: SyncOperationEntity): SyncResult {
        val incident = try {
            gson.fromJson(operation.payloadJson, Incident::class.java)
        } catch (e: Exception) {
            return SyncResult.Error("Error deserializando: ${e.message}")
        }

        return when (operation.operation) {
            "CREATE" -> performCreate(operation, incident)
            else -> SyncResult.Error("Operación no soportada")
        }
    }

    private suspend fun performCreate(operation: SyncOperationEntity, incident: Incident): SyncResult {
        return try {
            val request = IncidentRequest(
                titulo = incident.titulo,
                descripcion = incident.descripcion,
                categoria = incident.categoria.name,
                gravedad = incident.gravedad.name,
                maquinaId = incident.maquinaId,
                asignadoA = incident.asignadoA
            )
            val response = api.createIncident(request)
            if (response.isSuccessful) {
                response.body()?.let { dao.insertIncident(it.toEntity()) }
                SyncResult.Success
            } else {
                SyncResult.Retry("Error HTTP ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            SyncResult.Retry(e.message)
        }
    }
}
