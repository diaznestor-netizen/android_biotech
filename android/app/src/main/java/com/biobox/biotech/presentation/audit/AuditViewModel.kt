package com.biobox.biotech.presentation.audit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biobox.biotech.core.common.UiState
import com.biobox.biotech.data.remote.api.HistoryEntryDto
import com.biobox.biotech.data.remote.api.ReportService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuditViewModel @Inject constructor(private val reports: ReportService) : ViewModel() {
    private val _state = MutableStateFlow<UiState<List<HistoryEntryDto>>>(UiState.Loading)
    val state = _state.asStateFlow()

    init { load() }

    fun load() = viewModelScope.launch {
        _state.value = UiState.Loading
        runCatching { reports.getGlobalHistory() }
            .onSuccess { response ->
                _state.value = if (response.isSuccessful) UiState.Success(response.body()?.items.orEmpty())
                else UiState.Error("No fue posible consultar el historial (${response.code()})")
            }
            .onFailure { _state.value = UiState.Error(it.message ?: "No fue posible consultar el historial") }
    }
}
