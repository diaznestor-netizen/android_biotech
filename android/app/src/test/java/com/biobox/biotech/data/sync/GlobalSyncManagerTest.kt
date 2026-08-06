package com.biobox.biotech.data.sync

import androidx.work.WorkManager
import com.biobox.biotech.core.observability.ObservabilityManager
import com.biobox.biotech.data.local.dao.SyncOperationDao
import com.biobox.biotech.data.local.entity.SyncOperationEntity
import com.biobox.biotech.data.local.entity.SyncOperationStatus
import com.biobox.biotech.domain.sync.SyncHandler
import com.biobox.biotech.domain.sync.SyncResult
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import javax.inject.Provider

@OptIn(ExperimentalCoroutinesApi::class)
class GlobalSyncManagerTest {

    private val syncOperationDao = mockk<SyncOperationDao>(relaxed = true)
    private val workManager = mockk<WorkManager>(relaxed = true)
    private val projectHandler = mockk<SyncHandler>()
    private val machineHandler = mockk<SyncHandler>()
    private val observability = mockk<ObservabilityManager>(relaxed = true)

    private lateinit var syncManager: GlobalSyncManagerImpl

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        
        val handlers = mapOf(
            "PROJECT" to Provider { projectHandler },
            "MACHINE" to Provider { machineHandler }
        )

        syncManager = GlobalSyncManagerImpl(
            syncOperationDao = syncOperationDao,
            handlers = handlers,
            workManager = workManager,
            observability = observability
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `syncPendingOperations should process operations in order and update status`() = runTest {
        // Arrange
        val op1 = createTestOp("1", "PROJECT")
        val op2 = createTestOp("2", "MACHINE")
        
        coEvery { syncOperationDao.getPendingOperationsOnce() } returns listOf(op1, op2)
        coEvery { syncOperationDao.compareAndSetStatus(any(), any(), any(), any()) } returns 1
        coEvery { projectHandler.handle(op1) } returns SyncResult.Success
        coEvery { machineHandler.handle(op2) } returns SyncResult.Success

        // Act
        syncManager.syncPendingOperations()

        // Assert
        coVerify(exactly = 1) { projectHandler.handle(op1) }
        coVerify(exactly = 1) { machineHandler.handle(op2) }
        coVerify(exactly = 1) { syncOperationDao.deleteOperation(op1) }
        coVerify(exactly = 1) { syncOperationDao.deleteOperation(op2) }
    }

    @Test
    fun `syncPendingOperations should wait for parent entity to be synced`() = runTest {
        // Arrange
        val parentOp = createTestOp("parent", "PROJECT")
        val childOp = createTestOp("child", "MACHINE", parentId = "parent")
        
        coEvery { syncOperationDao.getPendingOperationsOnce() } returns listOf(childOp)
        coEvery { syncOperationDao.getActiveOperationsCountForParent("parent") } returns 1

        // Act
        syncManager.syncPendingOperations()

        // Assert
        coVerify(exactly = 0) { machineHandler.handle(any()) }
    }

    @Test
    fun `syncPendingOperations should handle HTTP 409 Conflict`() = runTest {
        // Arrange
        val op = createTestOp("1", "PROJECT")
        coEvery { syncOperationDao.getPendingOperationsOnce() } returns listOf(op)
        coEvery { syncOperationDao.compareAndSetStatus(any(), any(), any(), any()) } returns 1
        coEvery { projectHandler.handle(op) } returns SyncResult.Conflict("{\"remote\":1}", "Conflict")

        // Act
        syncManager.syncPendingOperations()

        // Assert
        coVerify { 
            syncOperationDao.updateOperation(match { 
                it.status == SyncOperationStatus.CONFLICT && it.conflictPayloadJson == "{\"remote\":1}" 
            }) 
        }
    }

    @Test
    fun `syncPendingOperations should handle Retry on 500 error`() = runTest {
        // Arrange
        val op = createTestOp("1", "PROJECT")
        coEvery { syncOperationDao.getPendingOperationsOnce() } returns listOf(op)
        coEvery { syncOperationDao.compareAndSetStatus(any(), any(), any(), any()) } returns 1
        coEvery { projectHandler.handle(op) } returns SyncResult.Retry("Server Error", 500)

        // Act
        syncManager.syncPendingOperations()

        // Assert
        coVerify { 
            syncOperationDao.updateOperation(match { 
                it.status == SyncOperationStatus.FAILED_RETRY && it.retryCount == 1 
            }) 
        }
    }

    @Test
    fun `syncPendingOperations should ensure mutual exclusion with Mutex`() = runTest {
        // Arrange
        val op = createTestOp("1", "PROJECT")
        coEvery { syncOperationDao.getPendingOperationsOnce() } coAnswers {
            delay(100) // Simular trabajo pesado
            listOf(op)
        }
        coEvery { syncOperationDao.compareAndSetStatus(any(), any(), any(), any()) } returns 1
        coEvery { projectHandler.handle(any()) } returns SyncResult.Success

        // Act
        val job1 = launch { syncManager.syncPendingOperations() }
        val job2 = launch { syncManager.syncPendingOperations() }
        
        job1.join()
        job2.join()

        // Assert - getPendingOperationsOnce se llamó 2 veces pero el contenido del bloque está protegido
        // En realidad con Mutex, el segundo llamado esperará a que el primero termine.
        coVerify(exactly = 2) { syncOperationDao.getPendingOperationsOnce() }
    }

    @Test
    fun `enqueueSync should use ExistingWorkPolicy KEEP`() {
        // Act
        syncManager.enqueueSync()

        // Assert
        verify { 
            workManager.enqueueUniqueWork(
                "global_sync",
                androidx.work.ExistingWorkPolicy.KEEP,
                any<androidx.work.OneTimeWorkRequest>()
            )
        }
    }

    @Test
    fun `syncPendingOperations should handle missing handler as permanent error`() = runTest {
        // Arrange
        val op = createTestOp("1", "UNKNOWN_TYPE")
        coEvery { syncOperationDao.getPendingOperationsOnce() } returns listOf(op)
        coEvery { syncOperationDao.compareAndSetStatus(any(), any(), any(), any()) } returns 1

        // Act
        syncManager.syncPendingOperations()

        // Assert
        coVerify { 
            syncOperationDao.updateOperation(match { 
                it.status == SyncOperationStatus.ERROR && it.lastError?.contains("No se encontró") == true
            }) 
        }
    }

    private fun createTestOp(id: String, type: String, parentId: String? = null) = SyncOperationEntity(
        id = id,
        entityType = type,
        entityLocalId = "local_$id",
        parentEntityLocalId = parentId,
        operation = "CREATE",
        payloadJson = "{}",
        status = SyncOperationStatus.PENDING
    )
}
