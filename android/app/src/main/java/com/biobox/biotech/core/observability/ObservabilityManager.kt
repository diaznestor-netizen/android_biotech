package com.biobox.biotech.core.observability

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObservabilityManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val crashlytics: FirebaseCrashlytics? by lazy {
        val defaultApp = FirebaseApp.getApps(context)
            .firstOrNull { it.name == FirebaseApp.DEFAULT_APP_NAME }
        if (defaultApp != null) FirebaseCrashlytics.getInstance() else null
    }

    fun setUserId(userId: String) {
        crashlytics?.setUserId(userId)
    }

    fun setCustomKey(key: String, value: String) {
        crashlytics?.setCustomKey(key, value)
    }

    fun setCustomKey(key: String, value: Int) {
        crashlytics?.setCustomKey(key, value)
    }

    fun setCustomKey(key: String, value: Long) {
        crashlytics?.setCustomKey(key, value)
    }

    fun log(message: String) {
        crashlytics?.log(message)
    }

    fun recordException(throwable: Throwable) {
        crashlytics?.recordException(throwable)
    }

    fun recordSyncConflict(entityType: String, entityId: String, traceId: String?) {
        log("Sync Conflict: type=$entityType, id=$entityId, traceId=$traceId")
        setCustomKey("entity_type", entityType)
        setCustomKey("sync_entity_id", entityId)
        setCustomKey("last_conflict_trace_id", traceId ?: "unknown")
    }

    fun recordSyncError(entityType: String, operationId: String, httpCode: Int?, traceId: String?) {
        setCustomKey("entity_type", entityType)
        setCustomKey("sync_operation_id", operationId)
        setCustomKey("last_http_code", httpCode ?: 0)
        setCustomKey("last_trace_id", traceId ?: "unknown")
    }
}
