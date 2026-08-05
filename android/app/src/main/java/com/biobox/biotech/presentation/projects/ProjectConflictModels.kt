package com.biobox.biotech.presentation.projects

import com.biobox.biotech.data.remote.dto.ProjectDto
import com.biobox.biotech.domain.model.ProjectPriority
import com.biobox.biotech.domain.model.ProjectStatus
import com.google.gson.Gson

data class ProjectConflictDetails(
    val remoteVersion: Int?,
    val differences: List<ProjectFieldDifference>,
    val remoteSnapshot: ProjectPresentationSnapshot
)

data class ProjectFieldDifference(
    val field: String,
    val localValue: String,
    val remoteValue: String
)

object ProjectConflictComparator {
    private val gson = Gson()

    fun parse(projectConflictPayloadJson: String?, local: ProjectPresentationSnapshot): ProjectConflictDetails? {
        if (projectConflictPayloadJson.isNullOrBlank()) return null
        val remote = runCatching {
            val root = gson.fromJson(projectConflictPayloadJson, Map::class.java)
            val serverProjectJson = gson.toJson(root["serverProject"])
            gson.fromJson(serverProjectJson, ProjectDto::class.java)
        }.getOrNull() ?: return null

        val differences = buildList {
            compare("Código", local.codigo, remote.codigo.orEmpty())
            compare("Nombre", local.nombre, remote.nombre.orEmpty())
            compare("Descripción", local.descripcion, remote.descripcion.orEmpty())
            compare("Cliente", local.cliente, remote.cliente.orEmpty())
            compare("Responsable", local.responsable, remote.responsable_nombre.orEmpty())
            compare("Estado", local.estado, remote.estado.orEmpty())
            compare("Prioridad", local.prioridad, remote.prioridad.orEmpty())
            compareDate("Fecha inicio", local.fechaInicio, remote.fecha_inicio)
            compareDate("Fecha fin estimada", local.fechaFinEstimada, remote.fecha_fin_estimada)
            compareDate("Fecha fin real", local.fechaFinReal, remote.fecha_fin_real)
            compare("Avance", local.porcentajeAvance, (remote.porcentaje_avance ?: 0).toString())
            compare("Observaciones", local.observaciones, remote.observaciones.orEmpty())
        }

        return ProjectConflictDetails(
            remoteVersion = remote.version,
            differences = differences,
            remoteSnapshot = ProjectPresentationSnapshot(
                codigo = remote.codigo.orEmpty(),
                nombre = remote.nombre.orEmpty(),
                descripcion = remote.descripcion.orEmpty(),
                cliente = remote.cliente.orEmpty(),
                responsable = remote.responsable_nombre.orEmpty(),
                estado = remote.estado.orEmpty(),
                prioridad = remote.prioridad.orEmpty(),
                fechaInicio = formatProjectDate(parseApiDate(remote.fecha_inicio.orEmpty())),
                fechaFinEstimada = formatProjectDate(parseApiDate(remote.fecha_fin_estimada.orEmpty())),
                fechaFinReal = formatProjectDate(parseApiDate(remote.fecha_fin_real.orEmpty())),
                porcentajeAvance = (remote.porcentaje_avance ?: 0).toString(),
                observaciones = remote.observaciones.orEmpty()
            )
        )
    }

    private fun MutableList<ProjectFieldDifference>.compare(field: String, local: String, remote: String) {
        if (local.trim() != remote.trim()) {
            add(ProjectFieldDifference(field = field, localValue = local.ifBlank { "Sin valor" }, remoteValue = remote.ifBlank { "Sin valor" }))
        }
    }

    private fun MutableList<ProjectFieldDifference>.compareDate(field: String, localFormatted: String, remoteApi: String?) {
        val localTimestamp = parseProjectDate(localFormatted)
        val remoteTimestamp = remoteApi?.let { parseApiDate(it) }

        if (localTimestamp != remoteTimestamp) {
            add(
                ProjectFieldDifference(
                    field = field,
                    localValue = localFormatted,
                    remoteValue = formatProjectDate(remoteTimestamp)
                )
            )
        }
    }
}

data class ProjectPresentationSnapshot(
    val codigo: String,
    val nombre: String,
    val descripcion: String,
    val cliente: String,
    val responsable: String,
    val estado: String,
    val prioridad: String,
    val fechaInicio: String,
    val fechaFinEstimada: String,
    val fechaFinReal: String,
    val porcentajeAvance: String,
    val observaciones: String
)

fun ProjectConflictDetails.toRemoteFormState(localId: String): ProjectFormState {
    return ProjectFormState(
        localId = localId,
        codigo = remoteSnapshot.codigo,
        nombre = remoteSnapshot.nombre,
        descripcion = remoteSnapshot.descripcion,
        cliente = remoteSnapshot.cliente,
        responsableNombre = remoteSnapshot.responsable,
        fechaInicio = remoteSnapshot.fechaInicio.parseProjectDate(),
        fechaFinEstimada = remoteSnapshot.fechaFinEstimada.parseProjectDate(),
        fechaFinReal = remoteSnapshot.fechaFinReal.parseProjectDate(),
        porcentajeAvance = remoteSnapshot.porcentajeAvance,
        estado = remoteSnapshot.estado.toProjectStatus(),
        prioridad = remoteSnapshot.prioridad.toProjectPriority(),
        observaciones = remoteSnapshot.observaciones,
        version = remoteVersion ?: 0
    )
}

private fun String.parseProjectDate(): Long? {
    return takeIf { it.isNotBlank() && it != "Sin fecha" }?.let { runCatching { parseProjectDate(it) }.getOrNull() }
}

private fun String.toProjectStatus(): ProjectStatus {
    return ProjectStatus.entries.firstOrNull { it.name.equals(this, ignoreCase = true) } ?: ProjectStatus.PLANEADO
}

private fun String.toProjectPriority(): ProjectPriority {
    return ProjectPriority.entries.firstOrNull { it.name.equals(this, ignoreCase = true) } ?: ProjectPriority.MEDIA
}
