package com.biobox.biotech.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.biobox.biotech.core.common.SyncStatus
import com.biobox.biotech.data.local.database.BioTechDatabase
import com.biobox.biotech.data.local.entity.EvidenceEntity
import com.biobox.biotech.data.local.entity.GoalEntity
import com.biobox.biotech.data.local.entity.MissionEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class OfflineEntitiesDaoTest {
    private lateinit var db: BioTechDatabase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, BioTechDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After fun close() = db.close()

    @Test
    fun pendingRecordsAreAvailableToTheSyncWorker() = runBlocking {
        db.missionDao().insertMission(MissionEntity(-1, "Misión", asignadoA = "Operador", fechaLimite = 2, prioridad = "ALTA", estado = "PENDIENTE", fechaCreacion = 1, syncStatus = SyncStatus.PENDING))
        db.goalDao().insertGoal(GoalEntity(-2, "Meta", fechaInicio = 1, estado = "NO_INICIADA", syncStatus = SyncStatus.PENDING))
        db.evidenceDao().insert(EvidenceEntity("ev-1", "ACTIVITY", "-3", "/data/user/0/app/files/evidence.jpg"))

        assertEquals(-1, db.missionDao().getPendingMissions().single().id)
        assertEquals(-2, db.goalDao().getPendingGoals().single().id)
        assertEquals("ev-1", db.evidenceDao().getPending().single().id)
    }
}
