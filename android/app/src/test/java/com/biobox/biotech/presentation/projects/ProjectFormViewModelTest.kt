package com.biobox.biotech.presentation.projects

import androidx.lifecycle.SavedStateHandle
import com.biobox.biotech.core.common.SyncStatus
import com.biobox.biotech.core.network.ApiResult
import com.biobox.biotech.domain.model.Project
import com.biobox.biotech.domain.model.ProjectPriority
import com.biobox.biotech.domain.model.ProjectStatus
import com.biobox.biotech.domain.repository.ProjectRepository
import com.biobox.biotech.domain.usecase.CreateProjectUseCase
import com.biobox.biotech.domain.usecase.GetProjectByLocalIdUseCase
import com.biobox.biotech.domain.usecase.ObserveProjectsUseCase
import com.biobox.biotech.domain.usecase.UpdateProjectUseCase
import com.biobox.biotech.domain.notifications.NotificationCenter
import com.biobox.biotech.domain.notifications.NotificationChannel
import com.biobox.biotech.domain.notifications.NotificationDispatcher
import com.biobox.biotech.domain.notifications.NotificationEvent
import com.biobox.biotech.domain.notifications.NotificationPriority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
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
class ProjectFormViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private val notificationCenter = NotificationCenter(
        NotificationDispatcher()
    )

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `restores project when localId exists`() = runTest {
        val repo = FakeProjectRepository(project())
        val vm = ProjectFormViewModel(
            SavedStateHandle(mapOf("localId" to "local-1")),
            GetProjectByLocalIdUseCase(repo),
            ObserveProjectsUseCase(repo),
            CreateProjectUseCase(repo),
            UpdateProjectUseCase(repo),
            notificationCenter
        )

        advanceUntilIdle()

        assertTrue(vm.state.value.isEditMode)
        assertEquals("PRJ-1", vm.state.value.form.codigo)
    }

    @Test
    fun `validates percentage and dates`() = runTest {
        val repo = FakeProjectRepository()
        val vm = ProjectFormViewModel(
            SavedStateHandle(),
            GetProjectByLocalIdUseCase(repo),
            ObserveProjectsUseCase(repo),
            CreateProjectUseCase(repo),
            UpdateProjectUseCase(repo),
            notificationCenter
        )
        vm.onCodigoChange("PRJ-1")
        vm.onNombreChange("Proyecto")
        vm.onFechaInicioChange(200L)
        vm.onFechaFinRealChange(100L)
        vm.onAvanceChange("150")
        vm.save()

        advanceUntilIdle()

        assertTrue(vm.state.value.form.fieldErrors.containsKey("fechaFinReal"))
        assertTrue(vm.state.value.form.fieldErrors.containsKey("porcentajeAvance"))
    }

    @Test
    fun `prevents double save while saving`() = runTest {
        val repo = FakeProjectRepository()
        val vm = ProjectFormViewModel(
            SavedStateHandle(),
            GetProjectByLocalIdUseCase(repo),
            ObserveProjectsUseCase(repo),
            CreateProjectUseCase(repo),
            UpdateProjectUseCase(repo),
            notificationCenter
        )
        vm.onCodigoChange("PRJ-1")
        vm.onNombreChange("Proyecto")
        vm.save()
        vm.save()

        advanceUntilIdle()

        assertEquals(1, repo.saveCalls)
    }

    @Test
    fun `restores draft from saved state and validates duplicate code`() = runTest {
        val repo = FakeProjectRepository(project("other-local", "DUP-1"))
        val vm = ProjectFormViewModel(
            SavedStateHandle(
                mapOf(
                    "draft_codigo" to "DUP-1",
                    "draft_nombre" to "Borrador"
                )
            ),
            GetProjectByLocalIdUseCase(repo),
            ObserveProjectsUseCase(repo),
            CreateProjectUseCase(repo),
            UpdateProjectUseCase(repo),
            notificationCenter
        )

        advanceUntilIdle()
        vm.save()
        advanceUntilIdle()

        assertEquals("DUP-1", vm.state.value.form.codigo)
        assertTrue(vm.state.value.form.fieldErrors["codigo"]?.contains("Ya existe") == true)
    }

    @Test
    fun `can apply remote value while merging conflict`() = runTest {
        val repo = FakeProjectRepository(
            project(
                localId = "local-1",
                codigo = "LOCAL-1",
                conflictPayloadJson = "{\"serverProject\":{\"codigo\":\"REM-9\",\"nombre\":\"Servidor\",\"estado\":\"PLANEADO\",\"prioridad\":\"MEDIA\",\"porcentaje_avance\":50},\"message\":\"409\"}",
                syncStatus = SyncStatus.CONFLICT
            )
        )
        val vm = ProjectFormViewModel(
            SavedStateHandle(mapOf("localId" to "local-1", "mergeConflict" to true)),
            GetProjectByLocalIdUseCase(repo),
            ObserveProjectsUseCase(repo),
            CreateProjectUseCase(repo),
            UpdateProjectUseCase(repo),
            notificationCenter
        )

        advanceUntilIdle()
        vm.useRemoteValue("Código")

        assertTrue(vm.state.value.conflict.isMergeMode)
        assertEquals("REM-9", vm.state.value.form.codigo)
    }

    private fun project(
        localId: String = "local-1",
        codigo: String = "PRJ-1",
        conflictPayloadJson: String? = null,
        syncStatus: SyncStatus = SyncStatus.SYNCED
    ) = Project(
        id = 1,
        localId = localId,
        codigo = codigo,
        nombre = "Proyecto",
        estado = ProjectStatus.PLANEADO,
        prioridad = ProjectPriority.MEDIA,
        syncStatus = syncStatus,
        conflictPayloadJson = conflictPayloadJson
    )

    private class FakeProjectRepository(
        project: Project? = null
    ) : ProjectRepository {
        private val flow = MutableStateFlow(project)
        var saveCalls = 0
        override fun getProjects(): Flow<List<Project>> = flowOf(listOfNotNull(flow.value))
        override fun getProjectByLocalId(localId: String): Flow<Project?> = flow
        override fun getPendingSyncCount() = flowOf(0)
        override suspend fun refreshProjects(query: String?) = ApiResult.Success(Unit)
        override suspend fun saveProject(project: Project): Result<Unit> {
            saveCalls++
            return Result.success(Unit)
        }
        override suspend fun updateProject(project: Project): Result<Unit> {
            saveCalls++
            return Result.success(Unit)
        }
        override suspend fun deleteProject(localId: String) = Result.success(Unit)
        override suspend fun retrySync(localId: String) = Result.success(Unit)
        override suspend fun resolveConflict(localId: String, useRemote: Boolean) = Result.success(Unit)
    }
}
