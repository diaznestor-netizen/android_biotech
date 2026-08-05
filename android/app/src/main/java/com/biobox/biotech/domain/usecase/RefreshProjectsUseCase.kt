package com.biobox.biotech.domain.usecase

import com.biobox.biotech.core.network.toResult
import com.biobox.biotech.domain.repository.ProjectRepository
import javax.inject.Inject

class RefreshProjectsUseCase @Inject constructor(
    private val repository: ProjectRepository
) {
    suspend operator fun invoke(query: String? = null): Result<Unit> {
        return repository.refreshProjects(query).toResult()
    }
}
