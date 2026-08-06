package com.biobox.biotech.data.repository

import com.biobox.biotech.data.remote.api.NotificationService
import com.biobox.biotech.data.remote.dto.TelegramMessageRequest
import com.biobox.biotech.domain.repository.NotificationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val notificationService: NotificationService
) : NotificationRepository {

    override suspend fun sendTelegramNotification(message: String, priority: String): Result<Unit> {
        return try {
            val response = notificationService.sendTelegramMessage(
                TelegramMessageRequest(message = message, priority = priority)
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                val errorMsg = response.body()?.error ?: response.message() ?: "Error desconocido al enviar a Telegram"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
