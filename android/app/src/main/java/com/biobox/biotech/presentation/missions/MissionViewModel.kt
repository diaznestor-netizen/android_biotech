package com.biobox.biotech.presentation.missions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biobox.biotech.core.common.UiState
import com.biobox.biotech.domain.model.Mission
import com.biobox.biotech.domain.repository.MissionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MissionViewModel @Inject constructor(
    private val missionRepository: MissionRepository
) : ViewModel() {

    private val _missions = MutableStateFlow<UiState<List<Mission>>>(UiState.Loading)
    val missions: StateFlow<UiState<List<Mission>>> = _missions.asStateFlow()

    private val _completedMissions = MutableStateFlow<UiState<List<Mission>>>(UiState.Idle)
    val completedMissions: StateFlow<UiState<List<Mission>>> = _completedMissions.asStateFlow()

    private val _currentMission = MutableStateFlow<UiState<Mission>>(UiState.Idle)
    val currentMission: StateFlow<UiState<Mission>> = _currentMission.asStateFlow()

    init { loadMissions() }

    fun loadMissions() {
        viewModelScope.launch {
            missionRepository.getMissions().collect { list ->
                _missions.value = UiState.Success(list)
            }
        }
        viewModelScope.launch { missionRepository.refreshMissions() }
    }

    fun loadCompletedMissions() {
        viewModelScope.launch {
            missionRepository.getCompletedMissions().collect { list ->
                _completedMissions.value = UiState.Success(list)
            }
        }
    }

    fun loadMission(id: Int) {
        viewModelScope.launch {
            missionRepository.getMissionById(id).collect { m ->
                if (m != null) _currentMission.value = UiState.Success(m)
            }
        }
    }

    fun createMission(mission: Mission, onSuccess: () -> Unit) {
        viewModelScope.launch {
            missionRepository.createMission(mission)
                .onSuccess { loadMissions(); onSuccess() }
                .onFailure { _missions.value = UiState.Error(it.message ?: "Error") }
        }
    }

    fun completeMission(id: Int, observaciones: String?) {
        viewModelScope.launch {
            missionRepository.completeMission(id, observaciones)
                .onSuccess { loadMissions() }
                .onFailure { _missions.value = UiState.Error(it.message ?: "Error") }
        }
    }

    fun approveMission(id: Int) {
        viewModelScope.launch {
            missionRepository.approveMission(id)
                .onSuccess { loadMissions() }
                .onFailure { _missions.value = UiState.Error(it.message ?: "Error") }
        }
    }
}
