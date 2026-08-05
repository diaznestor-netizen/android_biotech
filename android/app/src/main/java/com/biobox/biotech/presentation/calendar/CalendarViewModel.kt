package com.biobox.biotech.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biobox.biotech.core.common.UiState
import com.biobox.biotech.domain.model.CalendarEvent
import com.biobox.biotech.domain.repository.CalendarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val calendarRepository: CalendarRepository
) : ViewModel() {

    private val _events = MutableStateFlow<UiState<List<CalendarEvent>>>(UiState.Loading)
    val events: StateFlow<UiState<List<CalendarEvent>>> = _events.asStateFlow()

    fun loadEvents(startDate: Long, endDate: Long) {
        viewModelScope.launch {
            calendarRepository.getEvents(startDate, endDate).collect { list ->
                _events.value = UiState.Success(list)
            }
        }
        viewModelScope.launch { calendarRepository.refreshEvents(startDate, endDate) }
    }

    fun createEvent(event: CalendarEvent, onSuccess: () -> Unit) {
        viewModelScope.launch {
            calendarRepository.createEvent(event)
                .onSuccess { onSuccess() }
                .onFailure { _events.value = UiState.Error(it.message ?: "Error") }
        }
    }

    fun deleteEvent(id: Int) {
        viewModelScope.launch {
            calendarRepository.deleteEvent(id)
                .onSuccess { loadEvents(0, Long.MAX_VALUE) }
                .onFailure { _events.value = UiState.Error(it.message ?: "Error") }
        }
    }
}
