package com.biobox.biotech.core.common

sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Empty(val message: String) : UiState<Nothing>
    data class Error(
        val message: String,
        val canRetry: Boolean = true
    ) : UiState<Nothing>
}
