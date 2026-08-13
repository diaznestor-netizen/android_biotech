package com.biobox.biotech.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biobox.biotech.domain.model.User
import com.biobox.biotech.domain.repository.AuthRepository
import com.biobox.biotech.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    authRepository: AuthRepository
) : ViewModel() {

    val profile: StateFlow<User?> = authRepository.currentUser.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    private val _editMode = MutableStateFlow(false)
    val editMode: StateFlow<Boolean> = _editMode.asStateFlow()

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    // Eventos one-shot para errores y confirmaciones (Snackbar), sin pisar el estado
    private val _operationEvents = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val operationEvents: SharedFlow<String> = _operationEvents.asSharedFlow()

    fun startEdit() {
        _editMode.value = true
    }

    fun cancelEdit() {
        _editMode.value = false
    }

    fun saveProfile(nombre: String, apellido: String, email: String) {
        if (_saving.value) return
        val trimmedNombre = nombre.trim()
        val trimmedApellido = apellido.trim()
        val trimmedEmail = email.trim()
        if (trimmedNombre.isEmpty() || trimmedApellido.isEmpty()) {
            _operationEvents.tryEmit("Nombre y apellido son obligatorios")
            return
        }
        if (trimmedEmail.isNotEmpty() && !EMAIL_REGEX.matches(trimmedEmail)) {
            _operationEvents.tryEmit("El formato del email no es válido")
            return
        }
        viewModelScope.launch {
            _saving.value = true
            profileRepository.updateProfile(trimmedNombre, trimmedApellido, trimmedEmail)
                .onSuccess {
                    _editMode.value = false
                    _operationEvents.tryEmit("Perfil actualizado")
                }
                .onFailure { _operationEvents.tryEmit(it.message ?: "Error al actualizar el perfil") }
            _saving.value = false
        }
    }

    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String, onSuccess: () -> Unit = {}) {
        if (_saving.value) return
        if (currentPassword.isBlank()) {
            _operationEvents.tryEmit("Ingresa tu contraseña actual")
            return
        }
        if (newPassword.length < MIN_PASSWORD_LENGTH) {
            _operationEvents.tryEmit("La nueva contraseña debe tener al menos $MIN_PASSWORD_LENGTH caracteres")
            return
        }
        if (newPassword != confirmPassword) {
            _operationEvents.tryEmit("Las contraseñas no coinciden")
            return
        }
        viewModelScope.launch {
            _saving.value = true
            profileRepository.changePassword(currentPassword, newPassword)
                .onSuccess {
                    _operationEvents.tryEmit("Contraseña actualizada")
                    onSuccess()
                }
                .onFailure { _operationEvents.tryEmit(it.message ?: "Error al cambiar la contraseña") }
            _saving.value = false
        }
    }

    fun changePhone(newPhone: String, onSuccess: () -> Unit = {}) {
        if (_saving.value) return
        val digits = newPhone.filter { it.isDigit() }
        if (digits.length != PHONE_DIGITS) {
            _operationEvents.tryEmit("El teléfono debe tener $PHONE_DIGITS dígitos")
            return
        }
        viewModelScope.launch {
            _saving.value = true
            profileRepository.changePhone(digits)
                .onSuccess {
                    _operationEvents.tryEmit("Teléfono actualizado")
                    onSuccess()
                }
                .onFailure { _operationEvents.tryEmit(it.message ?: "Error al cambiar el teléfono") }
            _saving.value = false
        }
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 8
        private const val PHONE_DIGITS = 10
        private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    }
}
