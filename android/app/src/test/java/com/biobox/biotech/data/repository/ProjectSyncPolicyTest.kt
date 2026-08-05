package com.biobox.biotech.data.repository

import java.net.SocketTimeoutException
import org.junit.Assert.assertEquals
import org.junit.Test

class ProjectSyncPolicyTest {

    @Test
    fun `http 409 becomes conflict`() {
        assertEquals(SyncOutcomePolicy.Conflict, ProjectSyncPolicy.fromHttpCode(409))
    }

    @Test
    fun `http 400 becomes permanent error`() {
        assertEquals(SyncOutcomePolicy.PermanentError, ProjectSyncPolicy.fromHttpCode(400))
    }

    @Test
    fun `http 503 becomes retry`() {
        assertEquals(SyncOutcomePolicy.Retry, ProjectSyncPolicy.fromHttpCode(503))
    }

    @Test
    fun `timeout becomes retry`() {
        assertEquals(SyncOutcomePolicy.Retry, ProjectSyncPolicy.fromThrowable(SocketTimeoutException()))
    }
}
