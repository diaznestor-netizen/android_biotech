package com.biobox.biotech.data.repository

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class EvidenceFileCleanupTest {
    @Test
    fun deletesOnlyFromFilesEvidenceDirectory() {
        val root = Files.createTempDirectory("biotech-cleanup").toFile()
        val safe = File(root, "files/evidence/safe.jpg").apply { parentFile?.mkdirs(); writeText("safe") }
        val outside = File(root, "outside.jpg").apply { writeText("keep") }

        assertTrue(deleteSyncedEvidence(safe))
        assertFalse(safe.exists())
        assertFalse(deleteSyncedEvidence(outside))
        assertTrue(outside.exists())
    }
}
