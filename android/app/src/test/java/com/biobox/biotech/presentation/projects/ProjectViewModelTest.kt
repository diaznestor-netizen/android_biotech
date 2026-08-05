package com.biobox.biotech.presentation.projects

import com.biobox.biotech.core.common.SyncStatus
import com.biobox.biotech.core.network.ApiResult
import com.biobox.biotech.domain.model.Project
import com.biobox.biotech.domain.model.ProjectPriority
import com.biobox.biotech.domain.model.ProjectStatus
import com.biobox.biotech.domain.repository.ProjectRepository
import com.biobox.biotech.domain.usecase.CreateProjectUseCase
import com.biobox.biotech.domain.usecase.DeleteProjectUseCase
import com.biobox.biotech.domain.usecase.ObserveProjectsUseCase
import com.biobox.biotech.domain.usecase.RefreshProjectsUseCase
import com.biobox.biotech.domain.usecase.ResolveProjectConflictUseCase
import com.biobox.biotech.domain.usecase.RetryProjectSyncUseCase
import com.biobox.biotech.domain.usecase.SyncProjectsUseCase
import com.biobox.biotech.domain.usecase.UpdateProjectUseCase
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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
class ProjectViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val globalSyncManager = mockk<com.biobox.biotech.domain.sync.GlobalSyncManager>(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `debounce delays filtering until timeout`() = runTest {
        val repo = FakeProjectRepository(
            listOf(
                project("local-1", "ALFA"),
                project("local-2", "BETA")
            )
        )
        val vm = createVm(repo)
        advanceUntilIdle()

        vm.onSearchQueryChange("B")
        advanceTimeBy(200)
        assertEquals(2, vm.state.value.visibleProjects.size)

        advanceTimeBy(200)
        assertEquals(1, vm.state.value.visibleProjects.size)
        assertEquals("BETA", vm.state.value.visibleProjects.first().codigo)
    }

    @Test
    fun `search and status filter combine correctly`() = runTest {
        val repo = FakeProjectRepository(
            listOf(
                project("1", "ALFA", ProjectStatus.PLANEADO),
                project("2", "ALMACEN", ProjectStatus.FINALIZADO)
            )
        )
        val vm = createVm(repo)
        advanceUntilIdle()

        vm.onStatusFilterChange(ProjectStatus.FINALIZADO)
        vm.onSearchQueryChange("ALM")
        advanceTimeBy(400)

        assertEquals(1, vm.state.value.visibleProjects.size)
        assertEquals("2", vm.state.value.visibleProjects.first().localId)
    }

    @Test
    fun `resolve conflict keeps local strategy message`() = runTest {
        val repo = FakeProjectRepository(listOf(project("1", "ALFA", sync = SyncStatus.CONFLICT)))
        val vm = createVm(repo)
        advanceUntilIdle()
        val project = vm.state.value.projects.first()

        vm.askConflictResolution(project)
        vm.resolveConflict(useRemote = false)
        advanceUntilIdle()

        assertTrue(vm.state.value.message?.contains("versión local", ignoreCase = true) == true)
    }

    private fun createVm(repo: FakeProjectRepository) = ProjectViewModel(
        ObserveProjectsUseCase(repo),
        RefreshProjectsUseCase(repo),
        CreateProjectUseCase(repo),
        UpdateProjectUseCase(repo),
        DeleteProjectUseCase(repo),
        RetryProjectSyncUseCase(repo),
        ResolveProjectConflictUseCase(repo),
        SyncProjectsUseCase(globalSyncManager)
    )

    private fun project(localId: String, codigo: String, status: ProjectStatus = ProjectStatus.PLANEADO, sync: SyncStatus = SyncStatus.SYNCED) =
        Project(
            id = null,
            localId = localId,
            codigo = codigo,
            nombre = codigo,
            estado = status,
            prioridad = ProjectPriority.MEDIA,
            syncStatus = sync
        )

    private class FakeProjectRepository(projects: List<Project>) : ProjectRepository {
        private val flow = MutableStateFlow(projects)
        override fun getProjects(): Flow<List<Project>> = flow
        override fun getProjectByLocalId(localId: String): Flow<Project?> = flowOf(flow.value.firstOrNull { it.localId == localId })
        override fun getPendingSyncCount() = flowOf(flow.value.count { it.syncStatus != SyncStatus.SYNCED })
        override suspend fun refreshProjects(query: String?) = ApiResult.Success(Unit)
        override suspend fun saveProject(project: Project) = Result.success(Unit)
        override suspend fun updateProject(project: Project) = Result.success(Unit)
        override suspend fun deleteProject(localId: String) = Result.success(Unit)
        override suspend fun retrySync(localId: String) = Result.success(Unit)
        override suspend fun resolveConflict(localId: String, useRemote: Boolean) = Result.success(Unit)
    }
}
