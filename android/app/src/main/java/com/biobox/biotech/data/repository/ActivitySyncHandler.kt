package com.biobox.biotech.data.repository

import com.biobox.biotech.data.local.dao.ActivityDao
import com.biobox.biotech.data.local.entity.SyncOperationEntity
import com.biobox.biotech.data.mapper.toDomain
import com.biobox.biotech.data.mapper.toEntity
import com.biobox.biotech.data.remote.api.ActivityService
import com.biobox.biotech.data.remote.dto.ActivityRequest
import com.biobox.biotech.domain.model.Activity
import com.biobox.biotech.domain.sync.SyncHandler
import com.biobox.biotech.domain.sync.SyncResult
import com.google.gson.Gson
import javax.inject.Inject

class ActivitySyncHandler @Inject constructor(
    private val api: ActivityService,
    private val dao: ActivityDao
) : SyncHandler {
    private val gson = Gson()

    override suspend fun handle(operation: SyncOperationEntity): SyncResult {
        val activity = try {
            gson.fromJson(operation.payloadJson, Activity::class.java)
        } catch (e: Exception) {
            return SyncResult.Error("Error al deserializar actividad: ${e.message}")
        }

        return when (operation.operation) {
            "CREATE" -> performCreate(operation, activity)
            "UPDATE" -> performUpdate(operation, activity)
            else -> SyncResult.Error("Operación no soportada")
        }
    }

    private suspend fun performCreate(operation: SyncOperationEntity, activity: Activity): SyncResult {
        return try {
            val request = ActivityRequest(
                titulo = activity.titulo,
                descripcion = activity.descripcion,
                responsable = activity.responsable,
                maquinaId = activity.maquinaId,
                tiempoEmpleado = activity.tiempoEmpleado,
                fecha = activity.fecha,
                comentarios = activity.comentarios
            )
            val response = api.createActivity(request)
            if (response.isSuccessful) {
                response.body()?.let { dao.insertActivity(it.toEntity()) }
                SyncResult.Success
            } else {
                SyncResult.Retry("Error HTTP ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            SyncResult.Retry(e.message)
        }
    }

    private suspend fun performUpdate(operation: SyncOperationEntity, activity: Activity): SyncResult {
        return try {
            val request = ActivityRequest(
                titulo = activity.titulo,
                descripcion = activity.descripcion,
                responsable = activity.responsable,
                maquinaId = activity.maquinaId,
                tiempoEmpleado = activity.tiempoEmpleado,
                fecha = activity.fecha,
                comentarios = activity.comentarios
            )
            val response = api.updateActivity(activity.id, request)
            if (response.isSuccessful) {
                response.body()?.let { dao.insertActivity(it.toEntity()) }
                SyncResult.Success
            } else {
                SyncResult.Retry("Error HTTP ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            SyncResult.Retry(e.message)
        }
    }
}
