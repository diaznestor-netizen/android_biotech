package com.biobox.biotech.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TelegramMessageRequest(
    @SerializedName("message") val message: String,
    @SerializedName("chat_id") val chatId: String? = null,
    @SerializedName("priority") val priority: String = "NORMAL",
    @SerializedName("metadata") val metadata: Map<String, String>? = null
)

data class TelegramMessageResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message_id") val messageId: String?,
    @SerializedName("error") val error: String?
)

data class TelegramStatusResponse(
    @SerializedName("telegram_verified") val telegramVerified: Boolean = false,
    @SerializedName("telegram_username") val username: String? = null,
    @SerializedName("telegram_linked_at") val linkedAt: String? = null,
    @SerializedName("telegram_chat_id") val chatId: Long? = null,
    @SerializedName("is_linked") val isLinked: Boolean = false
)

data class TelegramLinkCodeResponse(
    @SerializedName("codigo") val linkCode: String,
    @SerializedName("mensaje") val message: String? = null,
    val botUsername: String = "BioTechBot"
)
