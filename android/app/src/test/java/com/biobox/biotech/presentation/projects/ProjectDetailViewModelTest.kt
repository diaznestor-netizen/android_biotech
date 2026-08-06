package com.biobox.biotech.presentation.projects

import androidx.lifecycle.SavedStateHandle
import com.biobox.biotech.core.common.SyncStatus
import com.biobox.biotech.core.network.ApiResult
import com.biobox.biotech.domain.model.Project
import com.biobox.biotech.domain.model.ProjectPriority
import com.biobox.biotech.domain.model.ProjectStatus
import com.biobox.biotech.domain.repository.ProjectRepository
import com.biobox.biotech.domain.usecase.DeleteProjectUseCase
import com.biobox.biotech.domain.usecase.GetProjectByLocalIdUseCase
import com.biobox.biotech.domain.usecase.RefreshProjectsUseCase
import com.biobox.biotech.domain.usecase.ResolveProjectConflictUseCase
import com.biobox.biotech.domain.usecase.RetryProjectSyncUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectDetailViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `opens detail with valid localId`() = runTest {
        val repo = FakeProjectRepository(project())
        val vm = createVm(repo, SavedStateHandle(mapOf("localId" to "local-1")))

        advanceUntilIdle()

        assertEquals("local-1", vm.state.value.project?.localId)
        assertEquals(null, vm.state.value.error)
    }

    @Test
    fun `shows not found for missing localId`() = runTest {
        val repo = FakeProjectRepository(null)
        val vm = createVm(repo, SavedStateHandle(mapOf("localId" to "missing")))

        advanceUntilIdle()

        assertTrue(vm.state.value.error?.contains("no encontrado", ignoreCase = true) == true)
    }

    @Test
    fun `navigates to manual merge once`() = runTest {
        val repo = FakeProjectRepository(project(syncStatus = SyncStatus.CONFLICT))
        val vm = createVm(repo, SavedStateHandle(mapOf("localId" to "local-1")))

        advanceUntilIdle()
        val events = mutableListOf<ProjectDetailEvent>()
        val job = launch { vm.events.collect { events += it } }

        vm.combineConflictManually()
        advanceUntilIdle()

        assertEquals(listOf(ProjectDetailEvent.NavigateToConflictMerge("local-1")), events)
        job.cancel()
    }

    @Test
    fun `emits navigate back once after delete`() = runTest {
        val repo = FakeProjectRepository(project())
        val vm = createVm(repo, SavedStateHandle(mapOf("localId" to "local-1")))

        advanceUntilIdle()
        val events = mutableListOf<ProjectDetailEvent>()
        val job = launch { vm.events.collect { events += it } }

        vm.deleteProject()
        advanceUntilIdle()

        assertEquals(listOf(ProjectDetailEvent.NavigateBack), events)
        job.cancel()
    }

    @Test
    fun `surfaces repository error without crash`() = runTest {
        val repo = FakeProjectRepository(project(), deleteResult = Result.failure(IllegalStateException("fallo repo")))
        val vm = createVm(repo, SavedStateHandle(mapOf("localId" to "local-1")))

        advanceUntilIdle()
        vm.deleteProject()
        advanceUntilIdle()

        assertTrue(vm.state.value.message?.contains("fallo repo", ignoreCase = true) == true)
    }

    private fun createVm(repo: FakeProjectRepository, savedStateHandle: SavedStateHandle) = ProjectDetailViewModel(
        savedStateHandle = savedStateHandle,
        getProjectByLocalIdUseCase = GetProjectByLocalIdUseCase(repo),
        refreshProjectsUseCase = RefreshProjectsUseCase(repo),
        deleteProjectUseCase = DeleteProjectUseCase(repo),
        retryProjectSyncUseCase = RetryProjectSyncUseCase(repo),
        resolveProjectConflictUseCase = ResolveProjectConflictUseCase(repo)
    )

    private fun project(syncStatus: SyncStatus = SyncStatus.SYNCED) = Project(
        id = 1,
        localId = "local-1",
        codigo = "PRJ-1",
        nombre = "Proyecto",
        estado = ProjectStatus.PLANEADO,
        prioridad = ProjectPriority.MEDIA,
        syncStatus = syncStatus
    )

    private class FakeProjectRepository(
        project: Project?,
        private val deleteResult: Result<Unit> = Result.success(Unit)
    ) : ProjectRepository {
        private val projectFlow = MutableStateFlow(project)

        override fun getProjects(): Flow<List<Project>> = flowOf(listOfNotNull(projectFlow.value))
        override fun getProjectByLocalId(localId: String): Flow<Project?> = projectFlow
        override fun getPendingSyncCount() = flowOf(0)
        override suspend fun refreshProjects(query: String?) = ApiResult.Success(Unit)
        override suspend fun saveProject(project: Project) = Result.success(Unit)
        override suspend fun updateProject(project: Project) = Result.success(Unit)
        override suspend fun deleteProject(localId: String) = deleteResult
        override suspend fun retrySync(localId: String) = Result.success(Unit)
        override suspend fun resolveConflict(localId: String, useRemote: Boolean) = Result.success(Unit)
    }
}
