package com.biobox.biotech.domain.usecase

import com.biobox.biotech.domain.repository.ProjectRepository
import javax.inject.Inject

class RetryProjectSyncUseCase @Inject constructor(
    private val repository: ProjectRepository
) {
    suspend operator fun invoke(localId: String): Result<Unit> = repository.retrySync(localId)
}
