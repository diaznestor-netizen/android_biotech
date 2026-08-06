package com.biobox.biotech.presentation.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biobox.biotech.core.common.UiState
import com.biobox.biotech.domain.model.Goal
import com.biobox.biotech.domain.repository.GoalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoalViewModel @Inject constructor(
    private val goalRepository: GoalRepository
) : ViewModel() {

    private val _goals = MutableStateFlow<UiState<List<Goal>>>(UiState.Loading)
    val goals: StateFlow<UiState<List<Goal>>> = _goals.asStateFlow()

    private val _currentGoal = MutableStateFlow<UiState<Goal>>(UiState.Idle)
    val currentGoal: StateFlow<UiState<Goal>> = _currentGoal.asStateFlow()

    init { loadGoals() }

    fun loadGoals() {
        viewModelScope.launch {
            goalRepository.getGoals().collect { list ->
                _goals.value = UiState.Success(list)
            }
        }
        viewModelScope.launch { goalRepository.refreshGoals() }
    }

    fun loadGoal(id: Int) {
        viewModelScope.launch {
            goalRepository.getGoalById(id).collect { g ->
                if (g != null) _currentGoal.value = UiState.Success(g)
            }
        }
    }

    fun createGoal(goal: Goal, onSuccess: () -> Unit) {
        viewModelScope.launch {
            goalRepository.createGoal(goal)
                .onSuccess { loadGoals(); onSuccess() }
                .onFailure { _goals.value = UiState.Error(it.message ?: "Error") }
        }
    }

    fun deleteGoal(id: Int) {
        viewModelScope.launch {
            goalRepository.deleteGoal(id)
                .onSuccess { loadGoals() }
                .onFailure { _goals.value = UiState.Error(it.message ?: "Error") }
        }
    }
}
