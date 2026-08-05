package com.biobox.biotech.domain.usecase

import com.biobox.biotech.domain.repository.ProjectRepository
import javax.inject.Inject

class DeleteProjectUseCase @Inject constructor(
    private val repository: ProjectRepository
) {
    suspend operator fun invoke(localId: String): Result<Unit> {
        return repository.deleteProject(localId)
    }
}
