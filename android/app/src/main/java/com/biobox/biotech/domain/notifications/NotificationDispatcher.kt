package com.biobox.biotech.domain.notifications

import com.biobox.biotech.domain.repository.NotificationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationDispatcher @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend fun dispatch(message: String, priority: NotificationPriority, channels: List<NotificationChannel>) {
        channels.forEach { channel ->
            when (channel) {
                NotificationChannel.TELEGRAM -> repository.sendTelegramNotification(message, priority.name)
                // Future channels would be handled here
                else -> { /* Not implemented yet */ }
            }
        }
    }
}
