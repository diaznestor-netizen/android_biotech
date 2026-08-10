package com.biobox.biotech.presentation.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biobox.biotech.core.common.UiState
import com.biobox.biotech.domain.model.Activity
import com.biobox.biotech.domain.repository.ActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    init { loadActivities() }

    fun loadActivities() {
        viewModelScope.launch {
            activityRepository.getActivities().collect { list ->
                _activities.value = UiState.Success(list)
            }
        }
        viewModelScope.launch { activityRepository.refreshActivities() }
    }

    fun loadActivity(id: Int) {
        viewModelScope.launch {
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
                .onFailure { _currentActivity.value = UiState.Error(it.message ?: "Error") }
        }
    }

    fun deleteActivityEvidence(activityId: Int, evidenceUrl: String) {
        viewModelScope.launch {
            activityRepository.deleteActivityEvidence(activityId, evidenceUrl)
                .onSuccess { loadActivity(activityId); loadActivities() }
                .onFailure { _currentActivity.value = UiState.Error(it.message ?: "Error") }
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
