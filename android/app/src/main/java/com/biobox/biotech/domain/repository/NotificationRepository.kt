package com.biobox.biotech.domain.repository

interface NotificationRepository {
    suspend fun sendTelegramNotification(message: String, priority: String = "NORMAL"): Result<Unit>
}
