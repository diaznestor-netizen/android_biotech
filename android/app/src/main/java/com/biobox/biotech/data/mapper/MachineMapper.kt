package com.biobox.biotech.data.mapper

import com.biobox.biotech.core.common.SyncStatus
import com.biobox.biotech.data.local.entity.MachineEntity
import com.biobox.biotech.data.remote.dto.MachineDto
import com.biobox.biotech.domain.model.Machine
import com.biobox.biotech.domain.model.MachineStatus

fun MachineDto.toEntityResult(): MappingResult<MachineEntity> {
    val validRemoteId = id ?: return MappingResult.Invalid("Remote ID is missing", codigo)
    return MappingResult.Success(MachineEntity(
        localId = validRemoteId.toString(), remoteId = validRemoteId,
        codigo = codigo ?: "SIN-CODIGO", nombre = nombre ?: "Máquina sin nombre",
        area = "Producción", estado = productionState ?: legacyState ?: "BORRADOR",
        porcentajeAvance = (progreso ?: 0.0).toInt(), imagenUrl = imagenUrl,
        responsable = responsable, ultimaRevision = null, syncStatus = SyncStatus.SYNCED
    ))
}

fun MachineEntity.toDomain(): Machine = Machine(
    id = remoteId ?: 0, codigo = codigo, nombre = nombre, area = area,
    estado = when (estado) {
        "TERMINADA", "ENTREGADA" -> MachineStatus.COMPLETA
        "CANCELADA" -> MachineStatus.NO_OPERATIVA
        else -> MachineStatus.INCOMPLETA
    },
    porcentajeAvance = porcentajeAvance, imagenUrl = imagenUrl, responsable = responsable,
    ultimaRevision = ultimaRevision, syncStatus = syncStatus
)

fun Machine.toEntity(localId: String? = null, syncStatus: SyncStatus = SyncStatus.SYNCED): MachineEntity =
    MachineEntity(
        localId = localId ?: (if (id > 0) id.toString() else java.util.UUID.randomUUID().toString()),
        remoteId = if (id > 0) id else null, codigo = codigo, nombre = nombre, area = area,
        estado = when (estado) {
            MachineStatus.COMPLETA -> "TERMINADA"
            MachineStatus.NO_OPERATIVA -> "CANCELADA"
            else -> "EN_PRODUCCION"
        },
        porcentajeAvance = porcentajeAvance, imagenUrl = imagenUrl, responsable = responsable,
        ultimaRevision = ultimaRevision, syncStatus = syncStatus
    )