package com.biobox.biotech.domain.sync

import kotlinx.coroutines.flow.StateFlow

interface GlobalSyncManager {
    suspend fun syncPendingOperations(): Result<Unit>
    fun enqueueSync()
    val syncStats: StateFlow<SyncStats>
}

data class SyncStats(
    val pendingCount: Int = 0,
    val errorCount: Int = 0,
    val conflictCount: Int = 0,
    val lastSyncTime: Long? = null,
    val isSyncing: Boolean = false
)
