package com.biobox.biotech.data.repository

import com.biobox.biotech.core.common.SyncStatus
import com.biobox.biotech.core.network.ApiResult
import com.biobox.biotech.data.local.dao.MachineDao
import com.biobox.biotech.data.local.dao.SyncOperationDao
import com.biobox.biotech.data.local.entity.SyncOperationEntity
import com.biobox.biotech.data.local.entity.SyncOperationStatus
import com.biobox.biotech.data.mapper.filterSuccess
import com.biobox.biotech.data.mapper.toDomain
import com.biobox.biotech.data.mapper.toEntity
import com.biobox.biotech.data.mapper.toEntityResult
import com.biobox.biotech.data.remote.api.MachineService
import com.biobox.biotech.data.remote.dto.*
import com.biobox.biotech.domain.model.Component
import com.biobox.biotech.domain.model.Machine
import com.biobox.biotech.domain.repository.*
import com.biobox.biotech.domain.sync.GlobalSyncManager
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MachineRepositoryImpl @Inject constructor(
    private val machineService: MachineService,
    private val machineDao: MachineDao,
    private val syncOperationDao: SyncOperationDao,
    private val globalSyncManager: GlobalSyncManager
) : MachineRepository {
    private val gson = Gson()

    override fun getMachines(): Flow<List<Machine>> =
        machineDao.getAllMachines().map { entities -> entities.map { it.toDomain() } }

    override fun getMachineById(id: Int): Flow<Machine?> =
        machineDao.getAllMachines().map { list -> list.find { it.remoteId == id }?.toDomain() }

    override fun getMachineByLocalId(localId: String): Flow<Machine?> =
        machineDao.getMachineByLocalId(localId).map { it?.toDomain() }

    override suspend fun refreshMachines(): ApiResult<Unit> = try {
        val response = machineService.getProductionMachines()
        if (!response.isSuccessful) ApiResult.HttpError(response.code(), response.message())
        else {
            val entities = response.body().orEmpty().map { it.toEntityResult() }.filterSuccess()
            machineDao.insertMachines(entities)
            if (entities.isNotEmpty()) machineDao.deleteStaleMachines(entities.mapNotNull { it.remoteId })
            ApiResult.Success(Unit)
        }
    } catch (e: Exception) { ApiResult.NetworkError(e) }

    override suspend fun refreshMachineProduction(id: Int): ApiResult<ProductionMachineDetail> = try {
        val machineResponse = machineService.getProductionMachine(id)
        if (!machineResponse.isSuccessful || machineResponse.body() == null) {
            ApiResult.HttpError(machineResponse.code(), machineResponse.message())
        } else {
            val componentsResponse = machineService.getProductionComponents(id)
            val completionResponse = machineService.getCompletionCheck(id)
            if (!componentsResponse.isSuccessful || !completionResponse.isSuccessful ||
                componentsResponse.body() == null || completionResponse.body() == null) {
                ApiResult.HttpError(if (!componentsResponse.isSuccessful) componentsResponse.code() else completionResponse.code(), "No se pudo cargar producción")
            } else {
                val machine = (machineResponse.body()!!.toEntityResult() as? com.biobox.biotech.data.mapper.MappingResult.Success)?.value?.toDomain() ?: return ApiResult.InvalidData("Máquina inválida")
                val components = componentsResponse.body()!!.map { dto ->
                    Component(dto.componentId ?: dto.id ?: 0, dto.nombre ?: "Componente", dto.state ?: "PENDIENTE",
                        dto.materiales.orEmpty().map { material ->
                            com.biobox.biotech.domain.model.Material(
                                id = material.id ?: 0, codigo = material.codigo.orEmpty(), nombre = material.nombre.orEmpty(),
                                cantidadRequerida = material.cantidadRequerida ?: 0, cantidadDisponible = material.cantidadDisponible ?: 0,
                                estado = material.estado ?: "Disponible", descripcion = material.descripcion, unidad = material.unidad,
                                stockMin = material.stock_min ?: 0.0, activo = material.activo ?: true
                            )
                        })
                }
                val completion = completionResponse.body()!!.let { CompletionCheck(it.progress, it.canBeFinished, it.missingComponents, it.missingMaterialIds, it.missingEvidence) }
                ApiResult.Success(ProductionMachineDetail(machine, components, completion))
            }
        }
    } catch (e: Exception) { ApiResult.NetworkError(e) }

    override suspend fun updateComponent(id: Int, componentId: Int, state: String, comment: String): ApiResult<CompletionCheck> = try {
        val response = machineService.updateComponent(id, componentId, UpdateComponentStateRequest(state, comment))
        if (!response.isSuccessful || response.body() == null) ApiResult.HttpError(response.code(), response.message())
        else response.body()!!.let { ApiResult.Success(CompletionCheck(it.progress, it.canBeFinished, it.missingComponents, it.missingMaterialIds, it.missingEvidence)) }
    } catch (e: Exception) { ApiResult.NetworkError(e) }

    override suspend fun transitionState(id: Int, state: String, comment: String): ApiResult<Unit> = try {
        val response = machineService.transitionState(id, TransitionMachineStateRequest(state, comment))
        if (response.isSuccessful) ApiResult.Success(Unit) else ApiResult.HttpError(response.code(), response.message())
    } catch (e: Exception) { ApiResult.NetworkError(e) }

    override suspend fun saveMachine(machine: Machine): Result<Unit> = runCatching {
        val localId = UUID.randomUUID().toString()
        val entity = machine.toEntity(localId, SyncStatus.PENDING)
        machineDao.insertMachine(entity)
        syncOperationDao.insertOperation(SyncOperationEntity(
            id = UUID.randomUUID().toString(), entityType = "MACHINE", entityLocalId = localId,
            operation = "CREATE", payloadJson = gson.toJson(machine), status = SyncOperationStatus.PENDING
        ))
        globalSyncManager.enqueueSync()
    }
}
