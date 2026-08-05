package com.biobox.biotech.domain.repository

import com.biobox.biotech.core.network.ApiResult
import com.biobox.biotech.domain.model.Component
import com.biobox.biotech.domain.model.Machine
import kotlinx.coroutines.flow.Flow

interface MachineRepository {
    fun getMachines(): Flow<List<Machine>>
    fun getMachineById(id: Int): Flow<Machine?>
    fun getMachineByLocalId(localId: String): Flow<Machine?>
    suspend fun refreshMachines(): ApiResult<Unit>
    suspend fun refreshMachineProduction(id: Int): ApiResult<ProductionMachineDetail>
    suspend fun updateComponent(id: Int, componentId: Int, state: String, comment: String = ""): ApiResult<CompletionCheck>
    suspend fun transitionState(id: Int, state: String, comment: String = ""): ApiResult<Unit>
    suspend fun saveMachine(machine: Machine): Result<Unit>
}

data class ProductionMachineDetail(
    val machine: Machine,
    val components: List<Component>,
    val completion: CompletionCheck
)

data class CompletionCheck(
    val progress: Double,
    val canBeFinished: Boolean,
    val missingComponents: List<String>,
    val missingMaterialIds: List<Int>,
    val missingEvidence: List<String>
)