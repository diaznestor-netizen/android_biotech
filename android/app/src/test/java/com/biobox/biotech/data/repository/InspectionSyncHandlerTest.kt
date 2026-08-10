package com.biobox.biotech.data.repository

import com.biobox.biotech.core.common.SyncStatus
import com.biobox.biotech.data.local.dao.EvidenceDao
import com.biobox.biotech.data.local.dao.InspectionDao
import com.biobox.biotech.data.local.entity.EvidenceEntity
import com.biobox.biotech.data.local.entity.InspectionEntity
import com.biobox.biotech.data.local.entity.SyncOperationEntity
import com.biobox.biotech.data.local.entity.SyncOperationStatus
import com.biobox.biotech.data.remote.api.InspectionService
import com.biobox.biotech.data.remote.dto.EvidenceUploadResponse
import com.biobox.biotech.data.remote.dto.InspectionResponse
import com.biobox.biotech.domain.sync.SyncResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Test
import retrofit2.Response
import java.io.File

class InspectionSyncHandlerTest {
    @Test
    fun createsInspectionUploadsEvidenceAndMarksItSynced() = runTest {
        val api = mockk<InspectionService>()
        val inspectionDao = mockk<InspectionDao>(relaxed = true)
        val evidenceDao = mockk<EvidenceDao>(relaxed = true)
        val photo = File.createTempFile("biotech-evidence", ".jpg").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        val inspection = InspectionEntity("local-1", 4, "[]", null, "[]", 1)
        val evidence = EvidenceEntity("ev-1", "INSPECTION", "local-1", photo.path)
        val operation = SyncOperationEntity("op-1", "INSPECTION", "local-1", operation = "SUBMIT", payloadJson = "", status = SyncOperationStatus.PENDING)

        coEvery { inspectionDao.getInspectionById("local-1") } returns inspection
        coEvery { evidenceDao.getPendingByOwner("INSPECTION", "local-1") } returns listOf(evidence)
        coEvery { api.submitInspection(any()) } returns Response.success(InspectionResponse(42))
        coEvery { api.uploadEvidence(42, any()) } returns Response.success(EvidenceUploadResponse(7, "/api/v1/evidence/7"))

        val result = InspectionSyncHandler(api, inspectionDao, evidenceDao).handle(operation)

        assertSame(SyncResult.Success, result)
        coVerify { inspectionDao.setRemoteId("local-1", 42) }
        coVerify { evidenceDao.updateSyncResult("ev-1", SyncStatus.SYNCED, "/api/v1/evidence/7", any()) }
        coVerify { inspectionDao.deleteInspection("local-1") }
        photo.delete()
    }
}
