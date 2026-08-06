package com.biobox.biotech.domain.usecase

import com.biobox.biotech.domain.model.Project
import com.biobox.biotech.domain.repository.ProjectRepository
import javax.inject.Inject

class UpdateProjectUseCase @Inject constructor(
    private val repository: ProjectRepository
) {
    suspend operator fun invoke(project: Project): Result<Unit> {
        if (project.nombre.isBlank()) return Result.failure(Exception("El nombre es obligatorio"))
        return repository.updateProject(project)
    }
}
