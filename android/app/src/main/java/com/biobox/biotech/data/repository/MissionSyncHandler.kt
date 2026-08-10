package com.biobox.biotech.data.repository

import com.biobox.biotech.data.local.dao.MissionDao
import com.biobox.biotech.data.local.entity.SyncOperationEntity
import com.biobox.biotech.data.mapper.toDomain
import com.biobox.biotech.data.mapper.toEntity
import com.biobox.biotech.data.remote.api.MissionService
import com.biobox.biotech.data.remote.dto.MissionRequest
import com.biobox.biotech.domain.model.Mission
import com.biobox.biotech.domain.sync.SyncHandler
import com.biobox.biotech.domain.sync.SyncResult
import com.google.gson.Gson
import javax.inject.Inject

class MissionSyncHandler @Inject constructor(
    private val api: MissionService,
    private val dao: MissionDao
) : SyncHandler {
    private val gson = Gson()

    override suspend fun handle(operation: SyncOperationEntity): SyncResult {
        val mission = try {
            gson.fromJson(operation.payloadJson, Mission::class.java)
        } catch (e: Exception) {
            return SyncResult.Error("Error deserializando: ${e.message}")
        }

        return when (operation.operation) {
            "CREATE" -> performCreate(operation, mission)
            "UPDATE" -> performUpdate(operation, mission)
            else -> SyncResult.Error("Operación no soportada")
        }
    }

    private suspend fun performCreate(operation: SyncOperationEntity, mission: Mission): SyncResult {
        return try {
            val request = MissionRequest(
                titulo = mission.titulo,
                descripcion = mission.descripcion,
                asignadoA = mission.asignadoA,
                maquinaId = mission.maquinaId,
                fechaLimite = mission.fechaLimite,
                prioridad = mission.prioridad.name
            )
            val response = api.createMission(request)
            if (response.isSuccessful) {
                val remote = response.body()?.toEntity()
                    ?: return SyncResult.Retry("Respuesta vacía al crear misión")
                dao.deleteMission(operation.entityLocalId.toIntOrNull() ?: mission.id)
                dao.insertMission(remote)
                SyncResult.Success
            } else {
                SyncResult.Retry("Error HTTP ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            SyncResult.Retry(e.message)
        }
    }

    private suspend fun performUpdate(operation: SyncOperationEntity, mission: Mission): SyncResult {
        return try {
            val request = MissionRequest(
                titulo = mission.titulo,
                descripcion = mission.descripcion,
                asignadoA = mission.asignadoA,
                maquinaId = mission.maquinaId,
                fechaLimite = mission.fechaLimite,
                prioridad = mission.prioridad.name
            )
            val response = api.updateMission(mission.id, request)
            if (response.isSuccessful) {
                response.body()?.let { dao.insertMission(it.toEntity()) }
                SyncResult.Success
            } else {
                SyncResult.Retry("Error HTTP ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            SyncResult.Retry(e.message)
        }
    }
}
