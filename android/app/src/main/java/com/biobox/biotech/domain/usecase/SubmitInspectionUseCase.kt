package com.biobox.biotech.domain.usecase

import com.biobox.biotech.domain.model.Inspection
import com.biobox.biotech.domain.repository.InspectionRepository
import javax.inject.Inject

class SubmitInspectionUseCase @Inject constructor(
    private val repository: InspectionRepository
) {
    suspend operator fun invoke(inspection: Inspection): Result<Unit> {
        val result = repository.submitInspection(inspection)
        return if (result.isSuccess) {
            result
        } else {
            // Save for later sync if it failed (likely network)
            repository.savePendingInspection(inspection)
            Result.success(Unit) // We return success to UI but indicate it's pending via repository state if needed
        }
    }
}
