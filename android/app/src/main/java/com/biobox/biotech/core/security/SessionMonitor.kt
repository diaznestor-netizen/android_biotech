package com.biobox.biotech.core.security

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionMonitor @Inject constructor() {
    companion object {
        private const val INACTIVITY_TIMEOUT_MS = 5 * 60 * 1000L
    }

    private var lastActivityTime = SystemClock.elapsedRealtime()
    private val handler = Handler(Looper.getMainLooper())
    private var onSessionExpired: (() -> Unit)? = null
    private var isMonitoring = false

    private val checkRunnable: Runnable = object : Runnable {
        override fun run() {
            val elapsed = SystemClock.elapsedRealtime() - lastActivityTime
            if (elapsed >= INACTIVITY_TIMEOUT_MS) {
                onSessionExpired?.invoke()
            } else {
                handler.postDelayed(this, INACTIVITY_TIMEOUT_MS - elapsed)
            }
        }
    }

    fun startMonitoring(onExpired: () -> Unit) {
        onSessionExpired = onExpired
        if (isMonitoring) return
        isMonitoring = true
        handler.post(checkRunnable)
    }

    fun stopMonitoring() {
        isMonitoring = false
        handler.removeCallbacks(checkRunnable)
        onSessionExpired = null
    }

    fun recordActivity() {
        lastActivityTime = SystemClock.elapsedRealtime()
    }

    fun isSessionExpired(): Boolean {
        return SystemClock.elapsedRealtime() - lastActivityTime >= INACTIVITY_TIMEOUT_MS
    }
}
