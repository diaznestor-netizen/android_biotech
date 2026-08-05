package com.biobox.biotech.domain.repository

import com.biobox.biotech.data.remote.dto.TelegramLinkCodeResponse
import com.biobox.biotech.data.remote.dto.TelegramStatusResponse
import com.biobox.biotech.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<User?>
    fun getPendingSyncCount(): Flow<Int>
    suspend fun login(phoneNumber: String, password: String): Result<User>
    suspend fun verifySecondFactor(sessionId: String, code: String): Result<User>
    suspend fun logout()
    suspend fun checkSession(): Boolean
    suspend fun refreshSession(): Result<Unit>
    suspend fun requestOtp(action: String): Result<Unit>
    suspend fun verifyOtp(action: String, code: String): Result<Unit>
    suspend fun getTelegramStatus(): Result<TelegramStatusResponse>
    suspend fun getLinkingCode(): Result<TelegramLinkCodeResponse>
    suspend fun unlinkTelegram(): Result<Unit>
    suspend fun requestPasswordRecovery(phoneNumber: String): Result<Unit>
    suspend fun confirmPasswordRecovery(phoneNumber: String, code: String, newPassword: String): Result<Unit>
}
