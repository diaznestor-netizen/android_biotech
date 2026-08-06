package com.biobox.biotech.data.repository

import com.biobox.biotech.data.local.dao.GoalDao
import com.biobox.biotech.data.local.entity.SyncOperationEntity
import com.biobox.biotech.data.mapper.toDomain
import com.biobox.biotech.data.mapper.toEntity
import com.biobox.biotech.data.remote.api.GoalService
import com.biobox.biotech.data.remote.dto.GoalRequest
import com.biobox.biotech.domain.model.Goal
import com.biobox.biotech.domain.sync.SyncHandler
import com.biobox.biotech.domain.sync.SyncResult
import com.google.gson.Gson
import javax.inject.Inject

class GoalSyncHandler @Inject constructor(
    private val api: GoalService,
    private val dao: GoalDao
) : SyncHandler {
    private val gson = Gson()

    override suspend fun handle(operation: SyncOperationEntity): SyncResult {
        val goal = try {
            gson.fromJson(operation.payloadJson, Goal::class.java)
        } catch (e: Exception) {
            return SyncResult.Error("Error al deserializar meta: ${e.message}")
        }

        return when (operation.operation) {
            "CREATE" -> performCreate(operation, goal)
            "UPDATE" -> performUpdate(operation, goal)
            "DELETE" -> performDelete(operation, goal)
            else -> SyncResult.Error("Operación no soportada")
        }
    }

    private suspend fun performCreate(operation: SyncOperationEntity, goal: Goal): SyncResult {
        return try {
            val request = GoalRequest(
                titulo = goal.titulo,
                descripcion = goal.descripcion,
                proyecto = goal.proyecto,
                maquinaId = goal.maquinaId,
                fechaInicio = goal.fechaInicio,
                fechaFin = goal.fechaFin
            )
            val response = api.createGoal(request)
            if (response.isSuccessful) {
                response.body()?.let { dao.insertGoal(it.toEntity()) }
                SyncResult.Success
            } else {
                SyncResult.Retry("Error HTTP ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            SyncResult.Retry(e.message)
        }
    }

    private suspend fun performUpdate(operation: SyncOperationEntity, goal: Goal): SyncResult {
        return try {
            val request = GoalRequest(
                titulo = goal.titulo,
                descripcion = goal.descripcion,
                proyecto = goal.proyecto,
                maquinaId = goal.maquinaId,
                fechaInicio = goal.fechaInicio,
                fechaFin = goal.fechaFin
            )
            val response = api.updateGoal(goal.id, request)
            if (response.isSuccessful) {
                response.body()?.let { dao.insertGoal(it.toEntity()) }
                SyncResult.Success
            } else {
                SyncResult.Retry("Error HTTP ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            SyncResult.Retry(e.message)
        }
    }

    private suspend fun performDelete(operation: SyncOperationEntity, goal: Goal): SyncResult {
        return try {
            val response = api.deleteGoal(goal.id)
            if (response.isSuccessful) {
                dao.deleteGoal(goal.id)
                SyncResult.Success
            } else {
                SyncResult.Retry("Error HTTP ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            SyncResult.Retry(e.message)
        }
    }
}
