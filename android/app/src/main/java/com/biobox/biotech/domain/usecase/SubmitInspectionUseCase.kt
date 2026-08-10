package com.biobox.biotech.domain.usecase

import com.biobox.biotech.domain.model.Inspection
import com.biobox.biotech.domain.repository.InspectionRepository
import javax.inject.Inject

class SubmitInspectionUseCase @Inject constructor(
    private val repository: InspectionRepository
) {
    suspend operator fun invoke(inspection: Inspection): Result<Unit> {
        return runCatching {
            repository.savePendingInspection(inspection)
            Unit
        }
    }
}
