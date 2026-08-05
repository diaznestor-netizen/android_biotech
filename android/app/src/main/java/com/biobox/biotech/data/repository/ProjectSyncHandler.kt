package com.biobox.biotech.data.repository

import androidx.room.withTransaction
import com.biobox.biotech.core.common.SyncStatus
import com.biobox.biotech.data.local.dao.ProjectDao
import com.biobox.biotech.data.local.database.BioTechDatabase
import com.biobox.biotech.data.local.entity.ProjectEntity
import com.biobox.biotech.data.local.entity.SyncOperationEntity
import com.biobox.biotech.data.mapper.*
import com.biobox.biotech.data.remote.api.ProjectService
import com.biobox.biotech.data.remote.dto.ProjectDto
import com.biobox.biotech.domain.sync.SyncHandler
import com.biobox.biotech.domain.sync.SyncResult
import com.google.gson.Gson
import okio.IOException
import retrofit2.HttpException
import java.net.SocketTimeoutException
import javax.inject.Inject

class ProjectSyncHandler @Inject constructor(
    private val projectService: ProjectService,
    private val projectDao: ProjectDao,
    private val database: BioTechDatabase
) : SyncHandler {

    private val gson = Gson()

    override suspend fun handle(operation: SyncOperationEntity): SyncResult {
        val entity = projectDao.getProjectByLocalId(operation.entityLocalId)
            ?: return SyncResult.Success

        return try {
            when (operation.operation) {
                "CREATE" -> performCreate(operation, entity)
                "UPDATE" -> performUpdate(operation, entity)
                "DELETE" -> performDelete(operation, entity)
                else -> SyncResult.Error("Operación desconocida: ${operation.operation}")
            }
        } catch (e: Exception) {
            classifyThrowable(e)
        }
    }

    private suspend fun performCreate(operation: SyncOperationEntity, entity: ProjectEntity): SyncResult {
        val response = projectService.createProject(
            idempotencyKey = operation.idempotencyKey ?: entity.localId,
            request = entity.toDomain().toCreateRequest()
        )
        return handleResponse(operation, entity, response.code(), response.body(), response.errorBody()?.string())
    }

    private suspend fun performUpdate(operation: SyncOperationEntity, entity: ProjectEntity): SyncResult {
        val remoteId = entity.remoteId ?: return performCreate(operation, entity)
        val response = projectService.updateProject(remoteId, entity.toDomain().toUpdateRequest())
        return handleResponse(operation, entity, response.code(), response.body(), response.errorBody()?.string())
    }

    private suspend fun performDelete(operation: SyncOperationEntity, entity: ProjectEntity): SyncResult {
        val remoteId = entity.remoteId ?: return SyncResult.Success
        val response = projectService.deleteProject(remoteId, entity.toDomain().toDeleteRequest())
        return when (response.code()) {
            in 200..299 -> SyncResult.Success
            409 -> handleConflict(operation, entity, response.body(), response.errorBody()?.string())
            400, 422 -> SyncResult.Error("Error de validación al eliminar", response.code())
            401 -> SyncResult.Retry("Sesión expirada", response.code())
            403 -> SyncResult.Error("Sin permisos para eliminar", response.code())
            else -> SyncResult.Retry("Error HTTP ${response.code()}", response.code())
        }
    }

    private suspend fun handleResponse(
        operation: SyncOperationEntity,
        entity: ProjectEntity,
        code: Int,
        body: ProjectDto?,
        errorMessage: String?
    ): SyncResult {
        return when (code) {
            in 200..299 -> {
                val dto = body ?: return SyncResult.Retry("Respuesta vacía")
                val remoteId = dto.id ?: return SyncResult.Retry("Falta ID remoto")
                val version = dto.version ?: 0
                database.withTransaction {
                    projectDao.markAsSynced(entity.localId, remoteId, version, SyncStatus.SYNCED, System.currentTimeMillis())
                }
                SyncResult.Success
            }
            400, 422 -> SyncResult.Error(errorMessage ?: "Error de validación", code)
            401 -> SyncResult.Retry("Sesión expirada", code)
            403 -> SyncResult.Error("Sin permisos", code)
            409 -> handleConflict(operation, entity, body, errorMessage)
            500, 503 -> SyncResult.Retry("Servidor no disponible", code)
            else -> SyncResult.Retry("Error HTTP $code", code)
        }
    }

    private fun handleConflict(
        operation: SyncOperationEntity,
        entity: ProjectEntity,
        serverProject: ProjectDto?,
        errorMessage: String?
    ): SyncResult {
        val resolvedServerProject = serverProject ?: errorMessage
            ?.takeIf { it.trim().startsWith("{") }
            ?.let { runCatching { gson.fromJson(it, ProjectDto::class.java) }.getOrNull() }
        
        val payload = SyncConflictPayload(
            localVersion = entity.version,
            operation = operation.operation,
            serverProject = resolvedServerProject,
            message = errorMessage
        )
        return SyncResult.Conflict(gson.toJson(payload), errorMessage ?: "Conflicto de versión")
    }

    private fun classifyThrowable(throwable: Throwable): SyncResult {
        return when (throwable) {
            is SocketTimeoutException, is IOException -> SyncResult.Retry(throwable.message ?: "Error de red")
            is HttpException -> SyncResult.Retry(throwable.message(), throwable.code())
            else -> SyncResult.Retry(throwable.message ?: "Error inesperado")
        }
    }
}
