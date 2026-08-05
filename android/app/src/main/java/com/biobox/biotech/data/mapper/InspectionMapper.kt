package com.biobox.biotech.data.mapper

import com.biobox.biotech.data.local.entity.InspectionEntity
import com.biobox.biotech.data.remote.dto.InspectionItemRequest
import com.biobox.biotech.data.remote.dto.InspectionRequest
import com.biobox.biotech.domain.model.Inspection
import com.biobox.biotech.domain.model.InspectionItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private val gson = Gson()

fun Inspection.toEntity(): InspectionEntity {
    return InspectionEntity(
        id = id,
        machineId = machineId,
        itemsJson = gson.toJson(items),
        observaciones = observaciones,
        evidencePathsJson = gson.toJson(evidencePaths),
        timestamp = timestamp
    )
}

fun InspectionEntity.toDomain(): Inspection {
    val itemsType = object : TypeToken<List<InspectionItem>>() {}.type
    val pathsType = object : TypeToken<List<String>>() {}.type
    
    return Inspection(
        id = id,
        machineId = machineId,
        items = gson.fromJson(itemsJson, itemsType),
        observaciones = observaciones,
        evidencePaths = gson.fromJson(evidencePathsJson, pathsType),
        timestamp = timestamp
    )
}

fun Inspection.toRequest(): InspectionRequest {
    return InspectionRequest(
        machineId = machineId,
        items = items.map { InspectionItemRequest(it.materialId, it.cantidadEncontrada) },
        observaciones = observaciones
    )
}
