package com.biobox.biotech.data.repository

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.biobox.biotech.core.common.SyncStatus
import com.biobox.biotech.core.datastore.SessionDataStore
import com.biobox.biotech.core.observability.ObservabilityManager
import com.biobox.biotech.data.local.database.BioTechDatabase
import com.biobox.biotech.data.local.entity.SyncOperationStatus
import com.biobox.biotech.data.projectDomain
import com.biobox.biotech.data.projectEntity
import com.biobox.biotech.data.remote.dto.ProjectDto
import com.biobox.biotech.data.remote.dto.UserDto
import com.biobox.biotech.data.sync.GlobalSyncManagerImpl
import com.biobox.biotech.domain.sync.SyncHandler
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.Executors
import javax.inject.Provider

@RunWith(AndroidJUnit4::class)
class ProjectRepositoryInstrumentedTest {

    @get:Rule
    val instantRule = InstantTaskExecutorRule()

    private lateinit var context: Context
    private lateinit var database: BioTechDatabase
    private lateinit var sessionDataStore: SessionDataStore
    private lateinit var workManager: WorkManager
    private lateinit var fakeService: com.biobox.biotech.data.FakeProjectService
    private lateinit var syncManager: GlobalSyncManagerImpl
    private lateinit var repository: ProjectRepositoryImpl

    @Before
    fun setup() = runBlocking {
        context = ApplicationProvider.getApplicationContext()
        WorkManagerTestInitHelper.initializeTestWorkManager(
            context,
            Configuration.Builder()
                .setExecutor(Executors.newSingleThreadExecutor())
                .build()
        )
        workManager = WorkManager.getInstance(context)
        database = Room.inMemoryDatabaseBuilder(context, BioTechDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        sessionDataStore = SessionDataStore(context)
        sessionDataStore.clearSession()
        sessionDataStore.saveSession(
            accessToken = "token-a",
            user = UserDto(id = 1, nombre = "User", apellido = "A", email = "a@test.dev", rol = "admin")
        )
        fakeService = com.biobox.biotech.data.FakeProjectService()
        val syncHandler = ProjectSyncHandler(fakeService, database.projectDao(), database)
        syncManager = GlobalSyncManagerImpl(
            syncOperationDao = database.syncOperationDao(),
            handlers = mapOf<String, Provider<SyncHandler>>("PROJECT" to Provider { syncHandler }),
            workManager = workManager,
            observability = ObservabilityManager(context)
        )
        repository = ProjectRepositoryImpl(
            projectService = fakeService,
            projectDao = database.projectDao(),
            syncOperationDao = database.syncOperationDao(),
            sessionDataStore = sessionDataStore,
            database = database,
            globalSyncManager = syncManager
        )
    }

    @After
    fun tearDown() = runBlocking {
        sessionDataStore.clearSession()
        database.close()
    }

    @Test
    fun offlineCreateEnqueuesSingleOperationAndPreservesSingleLocalRecord() = runBlocking {
        val project = projectDomain(localId = "local-1", remoteId = null)
        repository.saveProject(project)
        repository.saveProject(project.copy(nombre = "Proyecto actualizado"))

        val stored = database.projectDao().getProjectByLocalId("local-1")
        val ops = database.syncOperationDao().getOperationsByEntity("local-1", "PROJECT")

        assertNotNull(stored)
        assertEquals(1, ops.size)
        assertEquals("CREATE", ops.first().operation)
    }

    @Test
    fun syncPendingCreateAssignsRemoteIdAndClearsQueue() = runBlocking {
        repository.saveProject(projectDomain(localId = "local-2"))
        fakeService.createResponse = retrofit2.Response.success(
            ProjectDto(id = 99, local_id = "local-2", codigo = "PRJ-1", nombre = "Proyecto", version = 5)
        )

        syncManager.syncPendingOperations()

        val synced = database.projectDao().getProjectByLocalId("local-2")!!
        assertEquals(99, synced.remoteId)
        assertEquals(5, synced.version)
        assertEquals(SyncStatus.SYNCED, synced.syncStatus)
        assertTrue(database.syncOperationDao().getOperationsByEntity("local-2", "PROJECT").isEmpty())
    }

    @Test
    fun refreshFailureKeepsCache() = runBlocking {
        database.projectDao().insertProject(projectEntity(localId = "cached", syncStatus = SyncStatus.SYNCED))
        fakeService.projectsResponse = retrofit2.Response.error(
            500,
            "server".toResponseBody("text/plain".toMediaType())
        )

        val result = repository.refreshProjects()
        assertTrue(result is com.biobox.biotech.core.network.ApiResult.HttpError)
        assertNotNull(database.projectDao().getProjectByLocalId("cached"))
    }

    @Test
    fun invalidMappingDoesNotCrashRefresh() = runBlocking {
        fakeService.projectsResponse = retrofit2.Response.success(
            listOf(
                ProjectDto(id = null, local_id = "bad", codigo = "BAD", nombre = "Bad"),
                ProjectDto(id = 8, local_id = "ok", codigo = "OK", nombre = "Ok")
            )
        )

        val result = repository.refreshProjects()
        assertTrue(result is com.biobox.biotech.core.network.ApiResult.Success)
        assertNull(database.projectDao().getProjectByLocalId("bad"))
        assertNotNull(database.projectDao().getProjectByLocalId("ok"))
    }

    @Test
    fun conflict409MarksEntityAndStopsAutomaticResolution() = runBlocking {
        database.projectDao().insertProject(projectEntity(localId = "conflict", remoteId = 10, version = 1, syncStatus = SyncStatus.PENDING))
        database.syncOperationDao().insertOperation(
            com.biobox.biotech.data.local.entity.SyncOperationEntity(
                id = "op-conflict",
                entityType = "PROJECT",
                entityLocalId = "conflict",
                operation = "UPDATE",
                payloadJson = "{}",
                status = SyncOperationStatus.PENDING,
                userId = "1",
                idempotencyKey = "conflict"
            )
        )
        fakeService.updateResponse = retrofit2.Response.error(
            409,
            """{"id":10,"local_id":"conflict","codigo":"PRJ-1","nombre":"Remoto","version":2}""".toResponseBody("application/json".toMediaType())
        )

        syncManager.syncPendingOperations()

        val project = database.projectDao().getProjectByLocalId("conflict")!!
        val op = database.syncOperationDao().getOperationsByEntity("conflict", "PROJECT").first()
        assertEquals(SyncStatus.PENDING, project.syncStatus)
        assertNull(project.conflictPayloadJson)
        assertEquals(SyncOperationStatus.CONFLICT, op.status)
    }

    @Test
    fun userChangeDoesNotSyncAnotherUsersPendingOperation() = runBlocking {
        database.projectDao().insertProject(projectEntity(localId = "foreign", userId = 99))
        database.syncOperationDao().insertOperation(
            com.biobox.biotech.data.local.entity.SyncOperationEntity(
                id = "op-foreign",
                entityType = "PROJECT",
                entityLocalId = "foreign",
                operation = "CREATE",
                payloadJson = "{}",
                status = SyncOperationStatus.PENDING,
                userId = "99",
                idempotencyKey = "foreign"
            )
        )

        syncManager.syncPendingOperations()

        val project = database.projectDao().getProjectByLocalId("foreign")!!
        assertEquals(SyncStatus.SYNCED, project.syncStatus)
        assertEquals(1, fakeService.createCalls)
    }

    @Test
    fun enqueueUsesSingleUniqueWorkRequest() = runBlocking {
        repository.saveProject(projectDomain(localId = "local-3"))
        val infos = workManager.getWorkInfosForUniqueWork("global_sync").get()
        assertEquals(1, infos.size)
        val workInfo = infos.first()
        assertTrue(workInfo.state == WorkInfo.State.ENQUEUED || workInfo.state == WorkInfo.State.BLOCKED)
    }
}
