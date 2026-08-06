package com.biobox.biotech.data.repository

import com.biobox.biotech.core.common.SyncStatus
import com.biobox.biotech.data.local.dao.MachineDao
import com.biobox.biotech.data.local.entity.SyncOperationEntity
import com.biobox.biotech.data.remote.api.MachineService
import com.biobox.biotech.data.remote.dto.CreateMachineRequest
import com.biobox.biotech.domain.model.Machine
import com.biobox.biotech.domain.sync.SyncHandler
import com.biobox.biotech.domain.sync.SyncResult
import com.google.gson.Gson
import javax.inject.Inject

class MachineSyncHandler @Inject constructor(
    private val machineService: MachineService,
    private val machineDao: MachineDao
) : SyncHandler {
    private val gson = Gson()

    override suspend fun handle(operation: SyncOperationEntity): SyncResult {
        val machine = try {
            gson.fromJson(operation.payloadJson, Machine::class.java)
        } catch (e: Exception) {
            return SyncResult.Error("Error al deserializar máquina: ${e.message}")
        }

        return when (operation.operation) {
            "CREATE" -> performCreate(operation, machine)
            else -> SyncResult.Error("Operación no soportada: ${operation.operation}")
        }
    }

    private suspend fun performCreate(operation: SyncOperationEntity, machine: Machine): SyncResult {
        return try {
            val request = CreateMachineRequest(
                codigo = machine.codigo,
                nombre = machine.nombre,
                modelo = null,
                id_tipo = 1,
                id_responsable = null,
                descripcion = null
            )
            val response = machineService.createMachine(request)
            if (response.isSuccessful) {
                val remoteId = response.body()?.get("id") ?: 0
                machineDao.updateSyncMetadata(operation.entityLocalId, remoteId, SyncStatus.SYNCED)
                SyncResult.Success
            } else {
                SyncResult.Retry("Error HTTP ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            SyncResult.Retry(e.message)
        }
    }
}
