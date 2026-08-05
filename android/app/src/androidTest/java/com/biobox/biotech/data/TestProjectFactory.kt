package com.biobox.biotech.data

import com.biobox.biotech.core.common.SyncStatus
import com.biobox.biotech.data.local.entity.ProjectEntity
import com.biobox.biotech.data.remote.dto.CreateProjectRequest
import com.biobox.biotech.data.remote.dto.DeleteProjectRequest
import com.biobox.biotech.data.remote.dto.ProjectDto
import com.biobox.biotech.data.remote.dto.UpdateProjectRequest
import com.biobox.biotech.domain.model.Project
import com.biobox.biotech.domain.model.ProjectPriority
import com.biobox.biotech.domain.model.ProjectStatus

fun projectEntity(
    localId: String = "local-1",
    remoteId: Int? = null,
    codigo: String = "PRJ-1",
    syncStatus: SyncStatus = SyncStatus.PENDING,
    version: Int = 0,
    userId: Int? = 1,
    organizationId: String? = "org-a",
    tenantId: String? = "tenant-a"
) = ProjectEntity(
    localId = localId,
    remoteId = remoteId,
    codigo = codigo,
    nombre = "Proyecto",
    descripcion = "Demo",
    cliente = "Acme",
    responsableId = 7,
    responsableNombre = "Ana",
    usuarioCreadorId = userId,
    estado = "PLANEADO",
    prioridad = "MEDIA",
    fechaInicio = 100L,
    fechaFinEstimada = 200L,
    fechaFinReal = null,
    porcentajeAvance = 30,
    observaciones = null,
    version = version,
    syncStatus = syncStatus,
    organizationId = organizationId,
    tenantId = tenantId
)

fun projectDomain(
    localId: String = "local-1",
    remoteId: Int? = null,
    codigo: String = "PRJ-1",
    version: Int = 0,
    organizationId: String? = "org-a",
    tenantId: String? = "tenant-a"
) = Project(
    id = remoteId,
    localId = localId,
    codigo = codigo,
    nombre = "Proyecto",
    descripcion = "Demo",
    cliente = "Acme",
    responsableId = 7,
    responsableNombre = "Ana",
    usuarioCreadorId = 1,
    estado = ProjectStatus.PLANEADO,
    prioridad = ProjectPriority.MEDIA,
    fechaInicio = 100L,
    fechaFinEstimada = 200L,
    fechaFinReal = null,
    porcentajeAvance = 30,
    observaciones = null,
    version = version,
    syncStatus = SyncStatus.PENDING,
    organizationId = organizationId,
    tenantId = tenantId
)

class FakeProjectService : com.biobox.biotech.data.remote.api.ProjectService {
    var projectsResponse: retrofit2.Response<List<ProjectDto>> = retrofit2.Response.success(emptyList())
    var createResponse: retrofit2.Response<ProjectDto> = retrofit2.Response.success(ProjectDto(id = 1, local_id = "local-1", codigo = "PRJ-1", nombre = "Proyecto", version = 1))
    var updateResponse: retrofit2.Response<ProjectDto> = createResponse
    var deleteResponse: retrofit2.Response<ProjectDto> = createResponse
    var createCalls = 0
    var updateCalls = 0
    var deleteCalls = 0
    val createKeys = mutableListOf<String>()

    override suspend fun getProjects(query: String?) = projectsResponse

    override suspend fun getProjectById(id: Int) = retrofit2.Response.success(ProjectDto(id = id, local_id = "local-$id", codigo = "PRJ-$id", nombre = "Proyecto $id", version = 1))

    override suspend fun createProject(idempotencyKey: String, request: CreateProjectRequest): retrofit2.Response<ProjectDto> {
        createCalls++
        createKeys += idempotencyKey
        return createResponse
    }

    override suspend fun updateProject(id: Int, request: UpdateProjectRequest): retrofit2.Response<ProjectDto> {
        updateCalls++
        return updateResponse
    }

    override suspend fun deleteProject(id: Int, request: DeleteProjectRequest): retrofit2.Response<ProjectDto> {
        deleteCalls++
        return deleteResponse
    }
}
