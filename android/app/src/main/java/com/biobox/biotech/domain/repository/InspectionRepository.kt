package com.biobox.biotech.domain.repository

import com.biobox.biotech.domain.model.Inspection
import com.biobox.biotech.domain.model.InspectionSummary
import kotlinx.coroutines.flow.Flow

interface InspectionRepository {
    suspend fun getInspections(): Result<List<InspectionSummary>>
    suspend fun submitInspection(inspection: Inspection): Result<Unit>
    suspend fun savePendingInspection(inspection: Inspection)
    fun getPendingInspections(): Flow<List<Inspection>>
}

