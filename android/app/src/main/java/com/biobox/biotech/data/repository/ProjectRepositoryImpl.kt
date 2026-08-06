package com.biobox.biotech.data.repository

import androidx.room.withTransaction
import com.biobox.biotech.core.common.SyncStatus
import com.biobox.biotech.core.datastore.SessionDataStore
import com.biobox.biotech.core.network.ApiResult
import com.biobox.biotech.data.local.dao.ProjectDao
import com.biobox.biotech.data.local.dao.SyncOperationDao
import com.biobox.biotech.data.local.database.BioTechDatabase
import com.biobox.biotech.data.local.entity.ProjectEntity
import com.biobox.biotech.data.local.entity.SyncOperationEntity
import com.biobox.biotech.data.local.entity.SyncOperationStatus
import com.biobox.biotech.data.mapper.*
import com.biobox.biotech.data.remote.api.ProjectService
import com.biobox.biotech.domain.model.Project
import com.biobox.biotech.domain.repository.ProjectRepository
import com.biobox.biotech.domain.sync.GlobalSyncManager
import com.biobox.biotech.domain.usecase.ProjectValidation
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectRepositoryImpl @Inject constructor(
    private val projectService: ProjectService,
    private val projectDao: ProjectDao,
    private val syncOperationDao: SyncOperationDao,
    private val sessionDataStore: SessionDataStore,
    private val database: BioTechDatabase,
    private val globalSyncManager: GlobalSyncManager
) : ProjectRepository {

    private val gson = Gson()

    override fun getProjects(): Flow<List<Project>> =
        projectDao.getAllProjects().map { list -> list.map(ProjectEntity::toDomain) }

    override fun getProjectByLocalId(localId: String): Flow<Project?> =
        projectDao.observeProjectByLocalId(localId).map { it?.toDomain() }

    override fun getPendingSyncCount(): Flow<Int> = syncOperationDao.getPendingCount()

    override suspend fun refreshProjects(query: String?): ApiResult<Unit> = try {
        val response = projectService.getProjects(query)
        if (!response.isSuccessful) {
            ApiResult.HttpError(response.code(), response.message())
        } else {
            val currentContext = currentAccountContext()
            val entities = response.body().orEmpty()
                .map { dto -> dto.toEntityResult() }
                .filterSuccess()
                .map { entity ->
                    entity.copy(
                        organizationId = entity.organizationId ?: currentContext.organizationId,
                        tenantId = entity.tenantId ?: currentContext.tenantId,
                        syncStatus = SyncStatus.SYNCED,
                        conflictPayloadJson = null
                    )
                }
            database.withTransaction { projectDao.insertProjects(entities) }
            ApiResult.Success(Unit)
        }
    } catch (e: Exception) { ApiResult.NetworkError(e) }

    override suspend fun saveProject(project: Project): Result<Unit> = runCatching {
        val context = currentAccountContext()
        ProjectValidation.validate(project, isCreate = true).getOrThrow()
        val localId = project.localId.ifBlank { UUID.randomUUID().toString() }
        val entity = project.toPendingEntity(localId, context, null, 0)
            .copy(codigo = ProjectValidation.normalizeCode(project.codigo))
        
        val operation = buildOperation(entity, "CREATE", gson.toJson(entity.toDomain().toCreateRequest()), context)
        
        database.withTransaction {
            projectDao.insertProject(entity)
            syncOperationDao.insertOperation(operation)
        }
        globalSyncManager.enqueueSync()
    }

    override suspend fun updateProject(project: Project): Result<Unit> = runCatching {
        val context = currentAccountContext()
        ProjectValidation.validate(project, isCreate = false).getOrThrow()
        val existing = projectDao.getProjectByLocalId(project.localId) ?: error("Proyecto no encontrado")
        ensureAccountAccess(existing, context)
        
        val entity = project.toPendingEntity(existing.localId, context, existing.remoteId, existing.version)
            .copy(codigo = ProjectValidation.normalizeCode(project.codigo))
        
        val operation = buildOperation(
            entity, 
            if (existing.remoteId == null) "CREATE" else "UPDATE",
            gson.toJson(entity.toDomain().let { if (existing.remoteId == null) it.toCreateRequest() else it.toUpdateRequest() }),
            context
        )
        
        database.withTransaction {
            projectDao.updateProject(entity)
            syncOperationDao.insertOperation(operation)
        }
        globalSyncManager.enqueueSync()
    }

    override suspend fun deleteProject(localId: String): Result<Unit> = runCatching {
        val context = currentAccountContext()
        val existing = projectDao.getProjectByLocalId(localId) ?: error("Proyecto no encontrado")
        ensureAccountAccess(existing, context)
        
        if (existing.remoteId == null) {
            database.withTransaction {
                syncOperationDao.deleteByEntity("PROJECT", localId)
                projectDao.softDelete(localId, System.currentTimeMillis())
            }
        } else {
            val deletedEntity = existing.copy(
                syncStatus = SyncStatus.PENDING,
                deletedAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                conflictPayloadJson = null
            )
            val operation = buildOperation(deletedEntity, "DELETE", gson.toJson(deletedEntity.toDomain().toDeleteRequest()), context)
            database.withTransaction {
                projectDao.updateProject(deletedEntity)
                syncOperationDao.insertOperation(operation)
            }
            globalSyncManager.enqueueSync()
        }
    }

    override suspend fun retrySync(localId: String): Result<Unit> = runCatching {
        val ops = syncOperationDao.getOperationsByEntity(localId, "PROJECT")
        ops.filter { it.status == SyncOperationStatus.FAILED_RETRY || it.status == SyncOperationStatus.ERROR }.forEach {
            syncOperationDao.updateOperation(it.copy(
                status = SyncOperationStatus.PENDING,
                retryCount = 0,
                lastError = null,
                updatedAt = System.currentTimeMillis()
            ))
        }
        projectDao.updateSyncMetadata(localId, SyncStatus.PENDING, null, System.currentTimeMillis())
        globalSyncManager.enqueueSync()
    }

    override suspend fun resolveConflict(localId: String, useRemote: Boolean): Result<Unit> = runCatching {
        val entity = projectDao.getProjectByLocalId(localId) ?: error("Proyecto no encontrado")
        if (useRemote) {
            refreshProjects()
        } else {
            val updated = entity.copy(syncStatus = SyncStatus.PENDING, conflictPayloadJson = null)
            val operation = buildOperation(updated, "UPDATE", gson.toJson(updated.toDomain().toUpdateRequest()), currentAccountContext())
            database.withTransaction {
                projectDao.updateProject(updated)
                syncOperationDao.insertOperation(operation)
            }
            globalSyncManager.enqueueSync()
        }
    }

    private suspend fun currentAccountContext(): ProjectAccountContext {
        val user = sessionDataStore.userData.first()
        return ProjectAccountContext(userId = user?.id?.toString(), organizationId = null, tenantId = null)
    }

    private fun Project.toPendingEntity(localId: String, context: ProjectAccountContext, remoteId: Int?, version: Int) =
        toEntity().copy(
            localId = localId, remoteId = remoteId, version = version,
            syncStatus = SyncStatus.PENDING, conflictPayloadJson = null,
            organizationId = organizationId ?: context.organizationId,
            tenantId = tenantId ?: context.tenantId, updatedAt = System.currentTimeMillis()
        )

    private fun buildOperation(entity: ProjectEntity, operation: String, payloadJson: String, context: ProjectAccountContext) =
        SyncOperationEntity(
            id = UUID.randomUUID().toString(),
            entityType = "PROJECT",
            entityLocalId = entity.localId,
            operation = operation,
            payloadJson = payloadJson,
            status = SyncOperationStatus.PENDING,
            userId = entity.usuarioCreadorId?.toString() ?: context.userId,
            organizationId = entity.organizationId,
            tenantId = entity.tenantId,
            idempotencyKey = entity.localId,
            retryCount = 0
        )

    private fun ensureAccountAccess(entity: ProjectEntity, context: ProjectAccountContext) {
        val userMatches = entity.usuarioCreadorId == null || entity.usuarioCreadorId.toString() == context.userId
        if (!userMatches) error("La sesión actual no puede modificar este proyecto")
    }
}
