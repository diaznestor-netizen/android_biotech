package com.biobox.biotech.domain.repository

import com.biobox.biotech.domain.model.Incident
import kotlinx.coroutines.flow.Flow

interface IncidentRepository {
    fun getIncidents(): Flow<List<Incident>>
    fun getIncidentById(id: Int): Flow<Incident?>
    suspend fun refreshIncidents()
    suspend fun createIncident(incident: Incident): Result<Incident>
    suspend fun updateIncident(incident: Incident): Result<Incident>
    suspend fun resolveIncident(id: Int, comentarios: String): Result<Unit>
}
