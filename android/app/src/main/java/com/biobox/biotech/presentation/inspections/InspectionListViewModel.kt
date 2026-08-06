package com.biobox.biotech.presentation.inspections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biobox.biotech.core.common.UiState
import com.biobox.biotech.domain.model.InspectionSummary
import com.biobox.biotech.domain.repository.InspectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InspectionListViewModel @Inject constructor(
    private val inspectionRepository: InspectionRepository
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<List<InspectionSummary>>>(UiState.Loading)
    val state: StateFlow<UiState<List<InspectionSummary>>> = _state.asStateFlow()

    init { loadInspections() }

    fun loadInspections() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            inspectionRepository.getInspections()
                .onSuccess { _state.value = UiState.Success(it) }
                .onFailure { _state.value = UiState.Error(it.message ?: "No se pudieron cargar revisiones") }
        }
    }
}
