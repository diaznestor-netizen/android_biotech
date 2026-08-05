package com.biobox.biotech.data.remote.api

import com.biobox.biotech.data.remote.dto.TelegramMessageRequest
import com.biobox.biotech.data.remote.dto.TelegramMessageResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface NotificationService {
    @POST("notifications/telegram")
    suspend fun sendTelegramMessage(@Body request: TelegramMessageRequest): Response<TelegramMessageResponse>
}
