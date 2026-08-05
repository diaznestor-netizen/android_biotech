package com.biobox.biotech.presentation.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biobox.biotech.core.common.UiState
import com.biobox.biotech.domain.model.User
import com.biobox.biotech.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _users = MutableStateFlow<UiState<List<User>>>(UiState.Loading)
    val users: StateFlow<UiState<List<User>>> = _users.asStateFlow()

    private val _roles = MutableStateFlow<UiState<List<String>>>(UiState.Idle)
    val roles: StateFlow<UiState<List<String>>> = _roles.asStateFlow()

    init { loadUsers() }

    fun loadUsers() {
        viewModelScope.launch {
            userRepository.getUsers().collect { list ->
                _users.value = UiState.Success(list)
            }
        }
        viewModelScope.launch { userRepository.refreshUsers() }
    }

    fun createUser(user: User, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            userRepository.createUser(user, password)
                .onSuccess { loadUsers(); onSuccess() }
                .onFailure { _users.value = UiState.Error(it.message ?: "Error") }
        }
    }

    fun toggleUserActive(id: String, active: Boolean) {
        viewModelScope.launch {
            userRepository.toggleUserActive(id, active)
                .onSuccess { loadUsers() }
                .onFailure { _users.value = UiState.Error(it.message ?: "Error") }
        }
    }
}
