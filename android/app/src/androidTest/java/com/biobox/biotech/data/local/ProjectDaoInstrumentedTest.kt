package com.biobox.biotech.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.biobox.biotech.core.common.SyncStatus
import com.biobox.biotech.data.local.database.BioTechDatabase
import com.biobox.biotech.data.local.entity.SyncOperationStatus
import com.biobox.biotech.data.projectEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProjectDaoInstrumentedTest {

    private lateinit var db: BioTechDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, BioTechDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun createUpdateAndSyncPreservesLocalId() = runBlocking {
        db.projectDao().insertProject(projectEntity())
        val created = db.projectDao().getProjectByLocalId("local-1")
        assertNotNull(created)

        db.projectDao().updateProject(created!!.copy(nombre = "Editado"))
        db.projectDao().markAsSynced("local-1", remoteId = 77, version = 2, SyncStatus.SYNCED, System.currentTimeMillis())

        val synced = db.projectDao().getProjectByLocalId("local-1")!!
        assertEquals("local-1", synced.localId)
        assertEquals(77, synced.remoteId)
        assertEquals(2, synced.version)
        assertEquals(SyncStatus.SYNCED, synced.syncStatus)
    }

    @Test
    fun softDeleteAndPendingQueriesReflectStatus() = runBlocking {
        db.projectDao().insertProject(projectEntity(syncStatus = SyncStatus.PENDING))
        db.projectDao().softDelete("local-1", 999L)

        val deleted = db.projectDao().getProjectByLocalId("local-1")
        assertEquals(999L, deleted!!.deletedAt)
        assertTrue(db.projectDao().getAllProjects().first().isEmpty())
        assertEquals(1, db.projectDao().getPendingSincronization().first().size)
    }

    @Test
    fun metadataTransitionsToErrorAndConflictArePersisted() = runBlocking {
        db.projectDao().insertProject(projectEntity(syncStatus = SyncStatus.PENDING))

        db.projectDao().updateSyncMetadata("local-1", SyncStatus.FAILED, null, System.currentTimeMillis())
        assertEquals(SyncStatus.FAILED, db.projectDao().getProjectByLocalId("local-1")!!.syncStatus)

        db.projectDao().updateSyncMetadata("local-1", SyncStatus.CONFLICT, "{\"remoteVersion\":2}", System.currentTimeMillis())
        val conflict = db.projectDao().getProjectByLocalId("local-1")!!
        assertEquals(SyncStatus.CONFLICT, conflict.syncStatus)
        assertEquals("{\"remoteVersion\":2}", conflict.conflictPayloadJson)
    }

    @Test
    fun uniqueCodeIsScopedByOrganizationAndTenant() = runBlocking {
        db.projectDao().insertProject(projectEntity(localId = "a", codigo = "PRJ-X", organizationId = "org-a", tenantId = "t1"))
        db.projectDao().insertProject(projectEntity(localId = "b", codigo = "PRJ-X", organizationId = "org-b", tenantId = "t1"))

        val first = db.projectDao().getProjectByLocalId("a")
        val second = db.projectDao().getProjectByLocalId("b")
        assertNotNull(first)
        assertNotNull(second)
    }

    @Test(expected = android.database.sqlite.SQLiteConstraintException::class)
    fun duplicateCodeInsideSameOrganizationFails() = runBlocking {
        db.projectDao().insertProject(projectEntity(localId = "a", codigo = "PRJ-Z", organizationId = "org-a", tenantId = "t1"))
        db.projectDao().insertProject(projectEntity(localId = "b", codigo = "PRJ-Z", organizationId = "org-a", tenantId = "t1"))
    }

    @Test
    fun syncQueuePersistsProjectOperation() = runBlocking {
        db.projectDao().insertProject(projectEntity())
        db.syncOperationDao().insertOperation(
            com.biobox.biotech.data.local.entity.SyncOperationEntity(
                id = "op-1",
                entityType = "PROJECT",
                entityLocalId = "local-1",
                operation = "CREATE",
                payloadJson = "{}",
                status = SyncOperationStatus.PENDING,
                userId = "1",
                organizationId = "org-a",
                tenantId = "tenant-a",
                idempotencyKey = "local-1"
            )
        )
        val pending = db.syncOperationDao().getPendingOperationsOnce().filter { it.entityType == "PROJECT" }
        assertEquals(1, pending.size)
        assertEquals("local-1", pending.first().entityLocalId)
    }
}
