package com.biobox.biotech.data.sync

import androidx.work.*
import com.biobox.biotech.core.observability.ObservabilityManager
import com.biobox.biotech.data.local.dao.SyncOperationDao
import com.biobox.biotech.data.local.entity.SyncOperationStatus
import com.biobox.biotech.data.local.entity.SyncOperationEntity
import com.biobox.biotech.data.workers.BioTechSyncWorker
import com.biobox.biotech.domain.sync.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class GlobalSyncManagerImpl @Inject constructor(
    private val syncOperationDao: SyncOperationDao,
    private val handlers: Map<String, @JvmSuppressWildcards Provider<SyncHandler>>,
    private val workManager: WorkManager,
    private val observability: ObservabilityManager
) : GlobalSyncManager {

    private val mutex = Mutex()
    private val _syncStats = MutableStateFlow(SyncStats())
    override val syncStats: StateFlow<SyncStats> = _syncStats.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        observeStats()
    }

    private fun observeStats() {
        syncOperationDao.getPendingCount()
            .onEach { count ->
                _syncStats.update { it.copy(pendingCount = count) }
            }
            .launchIn(scope)
    }

    override suspend fun syncPendingOperations(): Result<Unit> = mutex.withLock {
        _syncStats.update { it.copy(isSyncing = true) }
        return try {
            val pendingOps = syncOperationDao.getPendingOperationsOnce()
            for (op in pendingOps) {
                // Verificar dependencias jerárquicas
                if (op.parentEntityLocalId != null) {
                    val activeParentOps = syncOperationDao.getActiveOperationsCountForParent(op.parentEntityLocalId)
                    if (activeParentOps > 0) {
                        // El padre aún tiene operaciones pendientes o en progreso
                        continue
                    }
                }

                // Marcar como en progreso
                val claimed = syncOperationDao.compareAndSetStatus(
                    op.id,
                    op.status,
                    SyncOperationStatus.IN_PROGRESS,
                    System.currentTimeMillis()
                )
                if (claimed == 0) continue

                val handlerProvider = handlers[op.entityType]
                if (handlerProvider == null) {
                    markPermanentError(op, "No se encontró un SyncHandler para ${op.entityType}", null)
                    continue
                }

                val startTime = System.currentTimeMillis()
                val result = try {
                    handlerProvider.get().handle(op)
                } catch (e: Exception) {
                    SyncResult.Retry(e.message)
                }
                val duration = System.currentTimeMillis() - startTime

                processResult(op, result, duration)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            _syncStats.update { 
                it.copy(
                    isSyncing = false, 
                    lastSyncTime = System.currentTimeMillis()
                ) 
            }
        }
    }

    private suspend fun processResult(op: SyncOperationEntity, result: SyncResult, duration: Long) {
        when (result) {
            is SyncResult.Success -> {
                syncOperationDao.deleteOperation(op)
            }
            is SyncResult.Retry -> {
                observability.recordSyncError(op.entityType, op.id, result.httpStatusCode, op.traceId)
                syncOperationDao.updateOperation(op.copy(
                    status = SyncOperationStatus.FAILED_RETRY,
                    retryCount = op.retryCount + 1,
                    lastError = result.message,
                    lastHttpStatusCode = result.httpStatusCode,
                    duration = duration,
                    updatedAt = System.currentTimeMillis()
                ))
            }
            is SyncResult.Conflict -> {
                observability.recordSyncConflict(op.entityType, op.entityLocalId, op.traceId)
                syncOperationDao.updateOperation(op.copy(
                    status = SyncOperationStatus.CONFLICT,
                    conflictPayloadJson = result.serverPayloadJson,
                    lastError = result.message,
                    duration = duration,
                    updatedAt = System.currentTimeMillis()
                ))
            }
            is SyncResult.Error -> {
                markPermanentError(op, result.message, result.httpStatusCode, duration)
            }
        }
    }

    private suspend fun markPermanentError(op: SyncOperationEntity, message: String, code: Int?, duration: Long? = null) {
        observability.recordSyncError(op.entityType, op.id, code, op.traceId)
        syncOperationDao.updateOperation(op.copy(
            status = SyncOperationStatus.ERROR,
            lastError = message,
            lastHttpStatusCode = code,
            duration = duration,
            updatedAt = System.currentTimeMillis()
        ))
    }

    override fun enqueueSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<BioTechSyncWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniqueWork(
            "global_sync",
            ExistingWorkPolicy.KEEP, // Lock distribuido: si ya hay uno en espera, mantenerlo
            syncRequest
        )
    }
}
