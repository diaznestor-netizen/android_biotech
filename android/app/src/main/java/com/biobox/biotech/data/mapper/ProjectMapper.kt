package com.biobox.biotech.data.mapper

import com.biobox.biotech.core.common.SyncStatus
import com.biobox.biotech.data.local.entity.ProjectEntity
import com.biobox.biotech.data.remote.dto.CreateProjectRequest
import com.biobox.biotech.data.remote.dto.DeleteProjectRequest
import com.biobox.biotech.data.remote.dto.ProjectDto
import com.biobox.biotech.data.remote.dto.UpdateProjectRequest
import com.biobox.biotech.domain.model.Project
import com.biobox.biotech.domain.model.ProjectPriority
import com.biobox.biotech.domain.model.ProjectStatus
import java.text.SimpleDateFormat
import java.util.*

private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

fun ProjectDto.toEntityResult(): MappingResult<ProjectEntity> {
    val id = id ?: return MappingResult.Invalid("Remote ID missing", codigo)
    val localId = local_id ?: id.toString()
    val validCodigo = codigo ?: return MappingResult.Invalid("Project code is required", localId)
    val validNombre = nombre ?: "Proyecto sin nombre"

    return MappingResult.Success(
        ProjectEntity(
            localId = localId,
            remoteId = id,
            codigo = validCodigo,
            nombre = validNombre,
            descripcion = descripcion,
            cliente = cliente,
            responsableId = responsable_id,
            responsableNombre = responsable_nombre,
            usuarioCreadorId = usuario_creador_id,
            estado = estado ?: "PLANEADO",
            prioridad = prioridad ?: "MEDIA",
            fechaInicio = fecha_inicio?.let { safeParseDate(it) },
            fechaFinEstimada = fecha_fin_estimada?.let { safeParseDate(it) },
            fechaFinReal = fecha_fin_real?.let { safeParseDate(it) },
            porcentajeAvance = porcentaje_avance ?: 0,
            observaciones = observaciones,
            version = version ?: 0,
            syncStatus = SyncStatus.SYNCED,
            conflictPayloadJson = null,
            createdAt = created_at ?: System.currentTimeMillis(),
            updatedAt = updated_at ?: System.currentTimeMillis()
        )
    )
}

fun ProjectEntity.toDomain(): Project {
    return Project(
        id = remoteId,
        localId = localId,
        codigo = codigo,
        nombre = nombre,
        descripcion = descripcion,
        cliente = cliente,
        responsableId = responsableId,
        responsableNombre = responsableNombre,
        usuarioCreadorId = usuarioCreadorId,
        estado = ProjectStatus.fromString(estado),
        prioridad = ProjectPriority.fromString(prioridad),
        fechaInicio = fechaInicio,
        fechaFinEstimada = fechaFinEstimada,
        fechaFinReal = fechaFinReal,
        porcentajeAvance = porcentajeAvance,
        observaciones = observaciones,
        version = version,
        createdAt = createdAt,
        updatedAt = updatedAt,
        syncStatus = syncStatus,
        conflictPayloadJson = conflictPayloadJson,
        organizationId = organizationId,
        tenantId = tenantId
    )
}

fun Project.toEntity(): ProjectEntity {
    return ProjectEntity(
        localId = localId,
        remoteId = id,
        codigo = codigo,
        nombre = nombre,
        descripcion = descripcion,
        cliente = cliente,
        responsableId = responsableId,
        responsableNombre = responsableNombre,
        usuarioCreadorId = usuarioCreadorId,
        estado = estado.name,
        prioridad = prioridad.name,
        fechaInicio = fechaInicio,
        fechaFinEstimada = fechaFinEstimada,
        fechaFinReal = fechaFinReal,
        porcentajeAvance = porcentajeAvance,
        observaciones = observaciones,
        version = version,
        syncStatus = syncStatus,
        conflictPayloadJson = conflictPayloadJson,
        createdAt = createdAt,
        updatedAt = updatedAt,
        organizationId = organizationId,
        tenantId = tenantId
    )
}

fun Project.toCreateRequest(): CreateProjectRequest {
    return CreateProjectRequest(
        local_id = localId,
        organization_id = organizationId,
        tenant_id = tenantId,
        codigo = codigo,
        nombre = nombre,
        descripcion = descripcion,
        cliente = cliente,
        responsable_id = responsableId,
        fecha_inicio = fechaInicio?.let { apiDateFormat.format(Date(it)) },
        fecha_fin_estimada = fechaFinEstimada?.let { apiDateFormat.format(Date(it)) },
        estado = estado.name,
        prioridad = prioridad.name,
        porcentaje_avance = porcentajeAvance
    )
}

fun Project.toUpdateRequest(): UpdateProjectRequest {
    return UpdateProjectRequest(
        id = id ?: 0,
        local_id = localId,
        organization_id = organizationId,
        tenant_id = tenantId,
        codigo = codigo,
        nombre = nombre,
        descripcion = descripcion,
        cliente = cliente,
        responsable_id = responsableId,
        estado = estado.name,
        prioridad = prioridad.name,
        fecha_inicio = fechaInicio?.let { apiDateFormat.format(Date(it)) },
        fecha_fin_estimada = fechaFinEstimada?.let { apiDateFormat.format(Date(it)) },
        fecha_fin_real = fechaFinReal?.let { apiDateFormat.format(Date(it)) },
        porcentaje_avance = porcentajeAvance,
        observaciones = observaciones,
        version = version
    )
}

fun Project.toDeleteRequest(): DeleteProjectRequest {
    return DeleteProjectRequest(
        local_id = localId,
        version = version,
        organization_id = organizationId,
        tenant_id = tenantId
    )
}

private fun safeParseDate(dateStr: String): Long? {
    return try {
        apiDateFormat.parse(dateStr)?.time
    } catch (e: Exception) {
        null
    }
}
