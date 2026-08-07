package com.biobox.biotech.presentation.auth

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biobox.biotech.core.common.UiState
import com.biobox.biotech.core.datastore.SessionDataStore
import com.biobox.biotech.core.security.BiometricAuth
import com.biobox.biotech.domain.model.User
import com.biobox.biotech.domain.notifications.NotificationCenter
import com.biobox.biotech.domain.notifications.NotificationEvent
import com.biobox.biotech.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val application: Application,
    private val biometricAuth: BiometricAuth,
    private val notificationCenter: NotificationCenter,
    private val sessionDataStore: SessionDataStore
) : ViewModel() {

    private val _loginState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val loginState: StateFlow<UiState<User>> = _loginState.asStateFlow()

    private val _sessionValidationState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val sessionValidationState: StateFlow<UiState<Boolean>> = _sessionValidationState.asStateFlow()

    val currentUser = authRepository.currentUser.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    val pendingSyncCount = authRepository.getPendingSyncCount().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val lastReAuthTime = sessionDataStore.lastReAuthTime.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = 0L
    )

    fun isReAuthRequired(currentTime: Long): Boolean {
        if (lastReAuthTime.value == 0L) return false
        val fourHoursMillis = 4 * 60 * 60 * 1000L
        return (currentTime - lastReAuthTime.value) > fourHoursMillis
    }

    val isBiometricAvailable: Boolean
        get() = biometricAuth.isAvailable(application)

    fun login(phoneNumber: String, password: String) {
        if (_loginState.value is UiState.Loading) return
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            authRepository.login(phoneNumber, password)
                .onSuccess { user ->
                    _loginState.value = UiState.Success(user)
                }
                .onFailure { error ->
                    _loginState.value = UiState.Error(error.message ?: "Error desconocido")
                    if (error.message?.contains("bloqueada", ignoreCase = true) == true) {
                        notificationCenter.notify(
                            NotificationEvent.AccountBlocked(
                                email = phoneNumber,
                                reason = "Múltiples intentos fallidos detectados desde Android"
                            )
                        )
                    }
                }
        }
    }

    fun register(phoneNumber: String, password: String, nombre: String, apellido: String, email: String) {
        if (_loginState.value is UiState.Loading) return
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            authRepository.register(phoneNumber, password, nombre, apellido, email)
                .onSuccess { user ->
                    _loginState.value = UiState.Success(user)
                }
                .onFailure { error ->
                    _loginState.value = UiState.Error(error.message ?: "Error desconocido")
                }
        }
    }

    fun reauthenticate(password: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            authRepository.reauthenticate(password)
                .onSuccess { _loginState.value = UiState.Success(it) }
                .onFailure { _loginState.value = UiState.Error(it.message ?: "Error desconocido") }
        }
    }

    fun validateSession() {
        if (_sessionValidationState.value is UiState.Loading) return
        viewModelScope.launch {
            _sessionValidationState.value = UiState.Loading
            val hasSession = authRepository.checkSession()
            if (!hasSession) {
                _sessionValidationState.value = UiState.Success(false)
                return@launch
            }

            authRepository.refreshSession()
                .onSuccess { _sessionValidationState.value = UiState.Success(true) }
                .onFailure {
                    authRepository.logout()
                    _sessionValidationState.value = UiState.Success(false)
                }
        }
    }

    fun consumeLoginState() {
        _loginState.value = UiState.Idle
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _loginState.value = UiState.Idle
            _sessionValidationState.value = UiState.Idle
        }
    }
}