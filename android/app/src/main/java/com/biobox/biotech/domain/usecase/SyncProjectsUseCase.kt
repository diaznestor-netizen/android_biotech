package com.biobox.biotech.domain.usecase

import com.biobox.biotech.domain.repository.ProjectRepository
import javax.inject.Inject

class SyncProjectsUseCase @Inject constructor(
    private val globalSyncManager: com.biobox.biotech.domain.sync.GlobalSyncManager
) {
    suspend operator fun invoke(): Result<Unit> = globalSyncManager.syncPendingOperations()
}
