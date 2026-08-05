package com.biobox.biotech.data.repository

import java.net.SocketTimeoutException

sealed interface SyncOutcomePolicy {
    data object Retry : SyncOutcomePolicy
    data object PermanentError : SyncOutcomePolicy
    data object Conflict : SyncOutcomePolicy
}

object ProjectSyncPolicy {
    fun fromHttpCode(code: Int): SyncOutcomePolicy {
        return when (code) {
            400, 422, 403 -> SyncOutcomePolicy.PermanentError
            409 -> SyncOutcomePolicy.Conflict
            401, 500, 503 -> SyncOutcomePolicy.Retry
            else -> SyncOutcomePolicy.Retry
        }
    }

    fun fromThrowable(throwable: Throwable): SyncOutcomePolicy {
        return when (throwable) {
            is SocketTimeoutException -> SyncOutcomePolicy.Retry
            else -> SyncOutcomePolicy.Retry
        }
    }
}
