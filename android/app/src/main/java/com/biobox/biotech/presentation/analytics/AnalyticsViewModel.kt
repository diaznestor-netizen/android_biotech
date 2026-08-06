package com.biobox.biotech.presentation.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biobox.biotech.core.common.UiState
import com.biobox.biotech.domain.model.AnalyticsData
import com.biobox.biotech.domain.repository.AnalyticsRepository
import com.biobox.biotech.domain.repository.DailySummaryData
import com.biobox.biotech.domain.repository.SummaryData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    private val _analyticsState = MutableStateFlow<UiState<AnalyticsData>>(UiState.Loading)
    val analyticsState: StateFlow<UiState<AnalyticsData>> = _analyticsState.asStateFlow()

    private val _dailySummary = MutableStateFlow<UiState<DailySummaryData>>(UiState.Idle)
    val dailySummary: StateFlow<UiState<DailySummaryData>> = _dailySummary.asStateFlow()

    private val _weeklySummary = MutableStateFlow<UiState<SummaryData>>(UiState.Idle)
    val weeklySummary: StateFlow<UiState<SummaryData>> = _weeklySummary.asStateFlow()

    private val _monthlySummary = MutableStateFlow<UiState<SummaryData>>(UiState.Idle)
    val monthlySummary: StateFlow<UiState<SummaryData>> = _monthlySummary.asStateFlow()

    init { loadAll() }

    fun loadAll() {
        viewModelScope.launch {
            _analyticsState.value = UiState.Loading
            analyticsRepository.getAnalytics()
                .onSuccess { _analyticsState.value = UiState.Success(it) }
                .onFailure { _analyticsState.value = UiState.Error(it.message ?: "Error") }
        }
    }

    fun loadDailySummary() {
        viewModelScope.launch {
            _dailySummary.value = UiState.Loading
            analyticsRepository.getDailySummary()
                .onSuccess { _dailySummary.value = UiState.Success(it) }
                .onFailure { _dailySummary.value = UiState.Error(it.message ?: "Error") }
        }
    }

    fun loadWeeklySummary() {
        viewModelScope.launch {
            _weeklySummary.value = UiState.Loading
            analyticsRepository.getWeeklySummary()
                .onSuccess { _weeklySummary.value = UiState.Success(it) }
                .onFailure { _weeklySummary.value = UiState.Error(it.message ?: "Error") }
        }
    }

    fun loadMonthlySummary() {
        viewModelScope.launch {
            _monthlySummary.value = UiState.Loading
            analyticsRepository.getMonthlySummary()
                .onSuccess { _monthlySummary.value = UiState.Success(it) }
                .onFailure { _monthlySummary.value = UiState.Error(it.message ?: "Error") }
        }
    }
}
