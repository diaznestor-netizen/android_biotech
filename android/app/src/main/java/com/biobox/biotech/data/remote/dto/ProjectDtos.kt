package com.biobox.biotech.data.remote.dto

data class ProjectDto(
    val id: Int?,
    val local_id: String?,
    val codigo: String?,
    val nombre: String?,
    val descripcion: String? = null,
    val cliente: String? = null,
    val responsable_id: Int? = null,
    val responsable_nombre: String? = null,
    val usuario_creador_id: Int? = null,
    val estado: String? = null,
    val prioridad: String? = null,
    val fecha_inicio: String? = null,      // Format YYYY-MM-DD
    val fecha_fin_estimada: String? = null,
    val fecha_fin_real: String? = null,
    val porcentaje_avance: Int? = 0,
    val observaciones: String? = null,
    val version: Int? = 0,
    val created_at: Long? = null,
    val updated_at: Long? = null,
    val organization_id: String? = null,
    val tenant_id: String? = null
)

data class CreateProjectRequest(
    val local_id: String,
    val organization_id: String? = null,
    val tenant_id: String? = null,
    val codigo: String,
    val nombre: String,
    val descripcion: String? = null,
    val cliente: String? = null,
    val responsable_id: Int? = null,
    val fecha_inicio: String? = null,
    val fecha_fin_estimada: String? = null,
    val estado: String? = "PLANEADO",
    val prioridad: String? = "MEDIA",
    val porcentaje_avance: Int = 0
)

data class UpdateProjectRequest(
    val id: Int,
    val local_id: String? = null,
    val organization_id: String? = null,
    val tenant_id: String? = null,
    val codigo: String? = null,
    val nombre: String? = null,
    val descripcion: String? = null,
    val cliente: String? = null,
    val responsable_id: Int? = null,
    val estado: String? = null,
    val prioridad: String? = null,
    val fecha_inicio: String? = null,
    val fecha_fin_estimada: String? = null,
    val fecha_fin_real: String? = null,
    val porcentaje_avance: Int? = null,
    val observaciones: String? = null,
    val version: Int
)

data class DeleteProjectRequest(
    val local_id: String,
    val version: Int,
    val organization_id: String? = null,
    val tenant_id: String? = null
)
