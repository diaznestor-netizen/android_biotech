package com.biobox.biotech.domain.usecase

import com.biobox.biotech.core.network.ApiResult
import com.biobox.biotech.domain.model.Project
import com.biobox.biotech.domain.model.ProjectPriority
import com.biobox.biotech.domain.model.ProjectStatus
import com.biobox.biotech.domain.repository.ProjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test

class ProjectUseCasesTest {

    @Test
    fun `create use case rejects invalid project`() = runBlocking {
        val useCase = CreateProjectUseCase(FakeProjectRepository())
        val result = useCase(project(codigo = ""))
        assertTrue(result.isFailure)
    }

    @Test
    fun `update use case delegates when valid`() = runBlocking {
        val repo = FakeProjectRepository()
        val result = UpdateProjectUseCase(repo)(project())
        assertTrue(result.isSuccess)
        assertTrue(repo.updateCalled)
    }

    private fun project(codigo: String = "PRJ-1") = Project(
        id = null,
        localId = "123e4567-e89b-12d3-a456-426614174000",
        codigo = codigo,
        nombre = "Proyecto",
        estado = ProjectStatus.PLANEADO,
        prioridad = ProjectPriority.MEDIA
    )

    private class FakeProjectRepository : ProjectRepository {
        var updateCalled = false
        override fun getProjects(): Flow<List<Project>> = flowOf(emptyList())
        override fun getProjectByLocalId(localId: String): Flow<Project?> = flowOf(null)
        override fun getPendingSyncCount() = flowOf(0)
        override suspend fun refreshProjects(query: String?) = ApiResult.Success(Unit)
        override suspend fun saveProject(project: Project) = Result.success(Unit)
        override suspend fun updateProject(project: Project): Result<Unit> {
            updateCalled = true
            return Result.success(Unit)
        }
        override suspend fun deleteProject(localId: String) = Result.success(Unit)
        override suspend fun retrySync(localId: String) = Result.success(Unit)
        override suspend fun resolveConflict(localId: String, useRemote: Boolean) = Result.success(Unit)
    }
}
