package com.biobox.biotech.data.mapper

import com.biobox.biotech.core.common.SyncStatus
import com.biobox.biotech.data.local.entity.MaterialEntity
import com.biobox.biotech.data.remote.dto.MaterialDto
import com.biobox.biotech.domain.model.Material

fun MaterialDto.toEntityResult(): MappingResult<MaterialEntity> {
    val validRemoteId = id ?: return MappingResult.Invalid("Remote ID is missing", codigo)
    
    return MappingResult.Success(
        MaterialEntity(
            localId = validRemoteId.toString(),
            remoteId = validRemoteId,
            codigo = codigo ?: "",
            nombre = nombre ?: "Material sin nombre",
            cantidadRequerida = cantidadRequerida ?: 0,
            cantidadDisponible = cantidadDisponible ?: 0,
            estado = estado ?: "Disponible",
            descripcion = descripcion,
            unidad = unidad,
            stockMin = stock_min ?: 0.0,
            activo = activo ?: true,
            syncStatus = SyncStatus.SYNCED
        )
    )
}

fun MaterialEntity.toDomain(): Material {
    return Material(
        id = remoteId,
        codigo = codigo,
        nombre = nombre,
        cantidadRequerida = cantidadRequerida,
        cantidadDisponible = cantidadDisponible,
        estado = estado,
        descripcion = descripcion,
        unidad = unidad,
        stockMin = stockMin,
        activo = activo,
        syncStatus = syncStatus
    )
}

fun Material.toEntity(localId: String? = null): MaterialEntity {
    return MaterialEntity(
        localId = localId ?: (id?.toString() ?: java.util.UUID.randomUUID().toString()),
        remoteId = id,
        codigo = codigo,
        nombre = nombre,
        cantidadRequerida = cantidadRequerida,
        cantidadDisponible = cantidadDisponible,
        estado = estado,
        descripcion = descripcion,
        unidad = unidad,
        stockMin = stockMin,
        activo = activo,
        syncStatus = syncStatus
    )
}
