package com.biobox.biotech.presentation.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biobox.biotech.core.common.UiState
import com.biobox.biotech.domain.model.Activity
import com.biobox.biotech.domain.repository.ActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val _activities = MutableStateFlow<UiState<List<Activity>>>(UiState.Loading)
    val activities: StateFlow<UiState<List<Activity>>> = _activities.asStateFlow()

    private val _currentActivity = MutableStateFlow<UiState<Activity>>(UiState.Idle)
    val currentActivity: StateFlow<UiState<Activity>> = _currentActivity.asStateFlow()

    // Eventos one-shot para errores de operaciones: no destruyen el estado de pantalla
    private val _operationEvents = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val operationEvents: SharedFlow<String> = _operationEvents.asSharedFlow()

    // Un solo colector activo por recurso: se cancela el anterior para evitar
    // colectores duplicados y race conditions entre detalles de distintas actividades
    private var listJob: Job? = null
    private var detailJob: Job? = null

    init { loadActivities() }

    fun loadActivities() {
        listJob?.cancel()
        listJob = viewModelScope.launch {
            activityRepository.getActivities().collect { list ->
                _activities.value = UiState.Success(list)
            }
        }
        viewModelScope.launch { activityRepository.refreshActivities() }
    }

    fun loadActivity(id: Int) {
        detailJob?.cancel()
        detailJob = viewModelScope.launch {
            activityRepository.getActivityById(id).collect { act ->
                if (act != null) _currentActivity.value = UiState.Success(act)
            }
        }
    }

    fun createActivity(activity: Activity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            activityRepository.createActivity(activity)
                .onSuccess { loadActivities(); onSuccess() }
                .onFailure { _activities.value = UiState.Error(it.message ?: "Error") }
        }
    }

    fun updateActivity(activity: Activity, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            activityRepository.updateActivity(activity)
                .onSuccess { loadActivity(activity.id); loadActivities(); onSuccess() }
                .onFailure { _operationEvents.tryEmit(it.message ?: "Error al actualizar la actividad") }
        }
    }

    fun deleteActivityEvidence(activityId: Int, evidenceUrl: String) {
        viewModelScope.launch {
            activityRepository.deleteActivityEvidence(activityId, evidenceUrl)
                .onFailure { _operationEvents.tryEmit(it.message ?: "Error al eliminar la foto") }
            // Room emite el cambio a través de los Flow ya activos: no hace falta recargar
        }
    }

    fun approveActivity(id: Int) {
        viewModelScope.launch {
            activityRepository.approveActivity(id)
                .onSuccess { loadActivities() }
                .onFailure { _activities.value = UiState.Error(it.message ?: "Error") }
        }
    }

    fun rejectActivity(id: Int, motivo: String) {
        viewModelScope.launch {
            activityRepository.rejectActivity(id, motivo)
                .onSuccess { loadActivities() }
                .onFailure { _activities.value = UiState.Error(it.message ?: "Error") }
        }
    }
}
