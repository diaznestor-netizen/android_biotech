package com.biobox.biotech.domain.usecase

import com.biobox.biotech.domain.model.Project
import com.biobox.biotech.domain.repository.ProjectRepository
import javax.inject.Inject

class CreateProjectUseCase @Inject constructor(
    private val repository: ProjectRepository
) {
    suspend operator fun invoke(project: Project): Result<Unit> {
        if (project.codigo.isBlank()) return Result.failure(Exception("El codigo es obligatorio"))
        if (project.nombre.isBlank()) return Result.failure(Exception("El nombre es obligatorio"))
        if (project.porcentajeAvance !in 0..100) return Result.failure(Exception("El porcentaje debe estar entre 0 y 100"))
        
        return repository.saveProject(project)
    }
}
