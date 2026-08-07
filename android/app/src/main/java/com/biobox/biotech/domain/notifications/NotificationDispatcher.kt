package com.biobox.biotech.domain.notifications

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationDispatcher @Inject constructor() {
    suspend fun dispatch(message: String, priority: NotificationPriority, channels: List<NotificationChannel>) {
        // No hay canales de entrega externos habilitados actualmente.
        // Los canales futuros (push, email, etc.) se gestionarían aquí.
    }
}