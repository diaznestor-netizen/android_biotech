package com.biobox.biotech.data.repository

import com.biobox.biotech.data.local.dao.IncidentDao
import com.biobox.biotech.data.local.dao.SyncOperationDao
import com.biobox.biotech.data.local.entity.SyncOperationEntity
import com.biobox.biotech.data.local.entity.SyncOperationStatus
import com.biobox.biotech.data.mapper.toDomain
import com.biobox.biotech.data.mapper.toEntity
import com.biobox.biotech.data.remote.api.IncidentService
import com.biobox.biotech.data.remote.dto.ResolveRequest
import com.biobox.biotech.domain.model.Incident
import com.biobox.biotech.domain.repository.IncidentRepository
import com.biobox.biotech.domain.sync.GlobalSyncManager
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class IncidentRepositoryImpl @Inject constructor(
    private val incidentService: IncidentService,
    private val incidentDao: IncidentDao,
    private val syncOperationDao: SyncOperationDao,
    private val globalSyncManager: GlobalSyncManager
) : IncidentRepository {

    private val gson = Gson()

    override fun getIncidents(): Flow<List<Incident>> {
        return incidentDao.getAllIncidents().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getIncidentById(id: Int): Flow<Incident?> {
        return incidentDao.getIncidentById(id).map { it?.toDomain() }
    }

    override suspend fun refreshIncidents() {
        try {
            val response = incidentService.getIncidents()
            if (response.isSuccessful) {
                val incidents = response.body().orEmpty().map { it.toEntity() }
                incidentDao.insertIncidents(incidents)
            }
        } catch (_: Exception) { }
    }

    override suspend fun createIncident(incident: Incident): Result<Incident> = runCatching {
        val entity = incident.toEntity()
        incidentDao.insertIncident(entity)
        syncOperationDao.insertOperation(SyncOperationEntity(
            id = UUID.randomUUID().toString(),
            entityType = "INCIDENT",
            entityLocalId = incident.id.toString(),
            operation = "CREATE",
            payloadJson = gson.toJson(incident),
            status = SyncOperationStatus.PENDING
        ))
        globalSyncManager.enqueueSync()
        incident
    }

    override suspend fun updateIncident(incident: Incident): Result<Incident> = runCatching {
        val entity = incident.toEntity()
        incidentDao.insertIncident(entity)
        syncOperationDao.insertOperation(SyncOperationEntity(
            id = UUID.randomUUID().toString(),
            entityType = "INCIDENT",
            entityLocalId = incident.id.toString(),
            operation = "UPDATE",
            payloadJson = gson.toJson(incident),
            status = SyncOperationStatus.PENDING
        ))
        globalSyncManager.enqueueSync()
        incident
    }

    override suspend fun resolveIncident(id: Int, comentarios: String): Result<Unit> = runCatching {
        val response = incidentService.resolveIncident(id, ResolveRequest(comentarios))
        if (!response.isSuccessful) throw Exception("Error al resolver: ${response.code()}")
        refreshIncidents()
    }
}
