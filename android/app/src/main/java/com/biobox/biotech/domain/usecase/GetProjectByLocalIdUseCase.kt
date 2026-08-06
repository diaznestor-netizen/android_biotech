package com.biobox.biotech.domain.usecase

import com.biobox.biotech.domain.model.Project
import com.biobox.biotech.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProjectByLocalIdUseCase @Inject constructor(
    private val repository: ProjectRepository
) {
    operator fun invoke(localId: String): Flow<Project?> = repository.getProjectByLocalId(localId)
}
