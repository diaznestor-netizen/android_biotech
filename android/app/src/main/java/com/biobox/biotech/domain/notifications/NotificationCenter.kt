package com.biobox.biotech.domain.notifications

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationCenter @Inject constructor(
    private val dispatcher: NotificationDispatcher
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun notify(event: NotificationEvent, channels: List<NotificationChannel> = emptyList()) {
        val (message, priority) = NotificationFormatter.format(event)
        
        scope.launch {
            dispatcher.dispatch(message, priority, channels)
        }
    }

    /**
     * Use this for critical system alerts that should bypass standard formatting if needed.
     */
    fun sendRawAlert(message: String, priority: NotificationPriority = NotificationPriority.HIGH) {
        scope.launch {
            dispatcher.dispatch(message, priority, emptyList())
        }
    }
}
