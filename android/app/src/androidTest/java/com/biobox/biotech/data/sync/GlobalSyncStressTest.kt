package com.biobox.biotech.data.sync

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.biobox.biotech.core.observability.ObservabilityManager
import com.biobox.biotech.data.local.dao.SyncOperationDao
import com.biobox.biotech.data.local.database.BioTechDatabase
import com.biobox.biotech.data.local.entity.SyncOperationEntity
import com.biobox.biotech.data.local.entity.SyncOperationStatus
import com.biobox.biotech.domain.sync.SyncHandler
import com.biobox.biotech.domain.sync.SyncResult
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import java.util.concurrent.Executors
import javax.inject.Provider
import kotlin.system.measureTimeMillis

@RunWith(AndroidJUnit4::class)
class GlobalSyncStressTest {

    private lateinit var db: BioTechDatabase
    private lateinit var syncOperationDao: SyncOperationDao
    private lateinit var syncManager: GlobalSyncManagerImpl
    private lateinit var workManager: WorkManager
    
    private val projectHandler = FakeSyncHandler()
    private val machineHandler = FakeSyncHandler()

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        WorkManagerTestInitHelper.initializeTestWorkManager(
            context,
            Configuration.Builder()
                .setExecutor(Executors.newSingleThreadExecutor())
                .build()
        )
        workManager = WorkManager.getInstance(context)
        db = Room.inMemoryDatabaseBuilder(context, BioTechDatabase::class.java).build()
        syncOperationDao = db.syncOperationDao()
        
        val handlers = mapOf<String, Provider<SyncHandler>>(
            "PROJECT" to Provider { projectHandler },
            "MACHINE" to Provider { machineHandler }
        )

        syncManager = GlobalSyncManagerImpl(
            syncOperationDao = syncOperationDao,
            handlers = handlers,
            workManager = workManager,
            observability = ObservabilityManager(context)
        )
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun stressTest_100_Operations_Integrity() = runBlocking {
        // Arrange: Insertar 100 operaciones
        val count = 100
        val ops = (1..count).map { i ->
            SyncOperationEntity(
                id = UUID.randomUUID().toString(),
                entityType = "PROJECT",
                entityLocalId = "p_$i",
                operation = "CREATE",
                payloadJson = "{}",
                status = SyncOperationStatus.PENDING
            )
        }
        
        ops.forEach { syncOperationDao.insertOperation(it) }
        
        // Simular latencia de red de 10ms por op
        projectHandler.resultFor = {
            delay(10)
            SyncResult.Success
        }

        // Act: Ejecutar sincronización y medir tiempo
        val time = measureTimeMillis {
            syncManager.syncPendingOperations()
        }

        // Assert: Cola vacía y todas exitosas
        val pendingCount = syncOperationDao.getPendingCount().first()
        assertEquals("La cola debería estar vacía", 0, pendingCount)
        println("Stress Test: 100 ops sincronizadas en ${time}ms")
    }

    @Test
    fun stressTest_Hierarchical_Dependency() = runBlocking {
        // Arrange: Proyecto (padre) y 10 Máquinas (hijos)
        val projectId = "project_1"
        syncOperationDao.insertOperation(SyncOperationEntity(
            id = "op_p1", entityType = "PROJECT", entityLocalId = projectId,
            operation = "CREATE", payloadJson = "{}", status = SyncOperationStatus.PENDING
        ))

        (1..10).forEach { i ->
            syncOperationDao.insertOperation(SyncOperationEntity(
                id = "op_m$i", entityType = "MACHINE", entityLocalId = "m_$i",
                parentEntityLocalId = projectId,
                operation = "CREATE", payloadJson = "{}", status = SyncOperationStatus.PENDING
            ))
        }

        // Simular fallo en el proyecto (Retry)
        projectHandler.resultFor = { SyncResult.Retry("Timeout") }
        machineHandler.resultFor = { SyncResult.Success }

        // Act
        syncManager.syncPendingOperations()

        // Assert: Las máquinas no deben haberse procesado porque el padre falló
        val pendingMachines = syncOperationDao.getPendingOperationsOnce().filter { it.entityType == "MACHINE" }
        assertEquals("Las 10 máquinas deberían seguir PENDING", 10, pendingMachines.size)
        
        // Ahora arreglamos el proyecto
        projectHandler.resultFor = { SyncResult.Success }
        
        // Act de nuevo
        syncManager.syncPendingOperations()
        
        // Assert final: Todo vacío
        assertEquals(0, syncOperationDao.getPendingCount().first())
    }

    @Test
    fun stressTest_Concurrent_Triggers() = runBlocking {
        // Arrange: 50 operaciones
        (1..50).forEach { i ->
            syncOperationDao.insertOperation(SyncOperationEntity(
                id = "op_$i", entityType = "PROJECT", entityLocalId = "p_$i",
                operation = "CREATE", payloadJson = "{}", status = SyncOperationStatus.PENDING
            ))
        }
        
        projectHandler.resultFor = {
            delay(5)
            SyncResult.Success
        }

        // Act: Disparar 5 sincronizaciones simultáneas
        val jobs = (1..5).map {
            launch(Dispatchers.Default) {
                syncManager.syncPendingOperations()
            }
        }
        jobs.joinAll()

        // Assert: El Mutex y compareAndSetStatus deben haber evitado duplicados
        assertEquals(0, syncOperationDao.getPendingCount().first())
    }

    private class FakeSyncHandler(
        var resultFor: suspend (SyncOperationEntity) -> SyncResult = { SyncResult.Success }
    ) : SyncHandler {
        override suspend fun handle(operation: SyncOperationEntity): SyncResult = resultFor(operation)
    }
}
