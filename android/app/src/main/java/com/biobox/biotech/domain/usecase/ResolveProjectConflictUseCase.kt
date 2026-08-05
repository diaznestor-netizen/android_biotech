package com.biobox.biotech.domain.usecase

import com.biobox.biotech.domain.repository.ProjectRepository
import javax.inject.Inject

class ResolveProjectConflictUseCase @Inject constructor(
    private val repository: ProjectRepository
) {
    suspend operator fun invoke(localId: String, useRemote: Boolean): Result<Unit> {
        return repository.resolveConflict(localId, useRemote)
    }
}
