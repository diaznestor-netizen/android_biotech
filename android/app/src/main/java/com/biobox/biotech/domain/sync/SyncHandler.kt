package com.biobox.biotech.domain.sync

import com.biobox.biotech.data.local.entity.SyncOperationEntity

interface SyncHandler {
    suspend fun handle(operation: SyncOperationEntity): SyncResult
}

sealed class SyncResult {
    object Success : SyncResult()
    data class Retry(val message: String? = null, val httpStatusCode: Int? = null) : SyncResult()
    data class Conflict(val serverPayloadJson: String?, val message: String?) : SyncResult()
    data class Error(val message: String, val httpStatusCode: Int? = null) : SyncResult()
}
