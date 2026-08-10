package com.biobox.biotech.data.repository

import com.biobox.biotech.core.common.SyncStatus
import com.biobox.biotech.data.local.dao.ActivityDao
import com.biobox.biotech.data.local.dao.EvidenceDao
import com.biobox.biotech.data.local.entity.EvidenceEntity
import com.biobox.biotech.data.local.entity.SyncOperationEntity
import com.biobox.biotech.data.local.entity.SyncOperationStatus
import com.biobox.biotech.data.remote.api.ActivityService
import com.biobox.biotech.data.remote.dto.ActivityDto
import com.biobox.biotech.data.remote.dto.EvidenceUploadResponse
import com.biobox.biotech.domain.model.Activity
import com.biobox.biotech.domain.model.ActivityStatus
import com.biobox.biotech.domain.sync.SyncResult
import com.google.gson.Gson
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Assert.assertFalse
import org.junit.Test
import retrofit2.Response
import java.io.File

class ActivitySyncHandlerTest {
    @Test
    fun createsActivityUploadsEvidenceAndMarksItSynced() = runTest {
        val api = mockk<ActivityService>()
        val activityDao = mockk<ActivityDao>(relaxed = true)
        val evidenceDao = mockk<EvidenceDao>(relaxed = true)
        val evidenceDir = java.nio.file.Files.createTempDirectory("biotech").toFile()
            .resolve("files/evidence").apply { mkdirs() }
        val photo = File(evidenceDir, "activity.jpg").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        val activity = Activity(-1, "Actividad", responsable = "Operador", tiempoEmpleado = 5, fecha = 1, estado = ActivityStatus.PENDIENTE)
        val evidence = EvidenceEntity("ev-1", "ACTIVITY", "-1", photo.path)
        val operation = SyncOperationEntity("op-1", "ACTIVITY", "-1", operation = "CREATE", payloadJson = Gson().toJson(activity), status = SyncOperationStatus.PENDING)
        val created = ActivityDto(42, "Actividad", responsable = "Operador", tiempoEmpleado = "5", fecha = "2026-08-10T12:00:00", estado = "PENDIENTE")

        coEvery { api.createActivity(any()) } returns Response.success(created)
        coEvery { evidenceDao.getPendingByOwner("ACTIVITY", "-1") } returns listOf(evidence)
        coEvery { api.uploadEvidence(42, any()) } returns Response.success(EvidenceUploadResponse(7, "/api/v1/evidence/7"))

        val result = ActivitySyncHandler(api, activityDao, evidenceDao).handle(operation)

        assertSame(SyncResult.Success, result)
        coVerify { evidenceDao.updateSyncResult("ev-1", SyncStatus.SYNCED, "/api/v1/evidence/7", any()) }
        coVerify { activityDao.deleteActivity(-1) }
        assertFalse(photo.exists())
    }
}
