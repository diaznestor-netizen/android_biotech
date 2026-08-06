package com.biobox.biotech.presentation.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biobox.biotech.data.remote.api.SystemService
import com.biobox.biotech.domain.sync.GlobalSyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class SyncStatusState(
    val isServerConnected: Boolean = true,
    val pendingOperationsCount: Int = 0,
    val errorCount: Int = 0,
    val conflictCount: Int = 0,
    val lastSyncTime: String = "Nunca",
    val isOnline: Boolean = true,
    val isSyncing: Boolean = false
)

@HiltViewModel
class SyncStatusViewModel @Inject constructor(
    private val systemService: SystemService,
    private val syncManager: GlobalSyncManager
) : ViewModel() {

    private val _connectionState = MutableStateFlow(true)
    private var failedHealthChecks = 0

    val state: StateFlow<SyncStatusState> = combine(
        syncManager.syncStats,
        _connectionState
    ) { stats, isConnected ->
        SyncStatusState(
            isServerConnected = isConnected,
            pendingOperationsCount = stats.pendingCount,
            errorCount = stats.errorCount,
            conflictCount = stats.conflictCount,
            lastSyncTime = stats.lastSyncTime?.let { formatTime(it) } ?: "Nunca",
            isOnline = isConnected,
            isSyncing = stats.isSyncing
        )
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SyncStatusState())

    init {
        startHealthCheck()
    }

    private fun startHealthCheck() {
        viewModelScope.launch {
            while (true) {
                try {
                    val response = systemService.health()
                    if (response.isSuccessful) {
                        failedHealthChecks = 0
                        _connectionState.value = true
                    } else {
                        failedHealthChecks += 1
                        if (failedHealthChecks >= 3) _connectionState.value = false
                    }
                } catch (e: Exception) {
                    failedHealthChecks += 1
                    if (failedHealthChecks >= 3) _connectionState.value = false
                }
                delay(10000) // Cada 10 segundos
            }
        }
    }

    private fun formatTime(time: Long): String {
        return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(time))
    }

    fun triggerSync() {
        syncManager.enqueueSync()
    }
}
