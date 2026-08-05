package com.biobox.biotech.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biobox.biotech.core.common.UiState
import com.biobox.biotech.domain.model.Machine
import com.biobox.biotech.domain.model.MachineStatus
import com.biobox.biotech.domain.repository.MachineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardData(
    val machines: List<Machine> = emptyList(),
    val totalMachines: Int = 0,
    val completedMachines: Int = 0,
    val incompleteMachines: Int = 0,
    val averageProgress: Int = 0
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val machineRepository: MachineRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<DashboardData>>(UiState.Loading)
    val uiState: StateFlow<UiState<DashboardData>> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun refresh() {
        loadDashboard()
    }

    private fun loadDashboard() {
        machineRepository.getMachines()
            .onEach { machines ->
                val data = DashboardData(
                    machines = machines,
                    totalMachines = machines.size,
                    completedMachines = machines.count { it.estado == MachineStatus.COMPLETA },
                    incompleteMachines = machines.count { it.estado != MachineStatus.COMPLETA },
                    averageProgress = if (machines.isEmpty()) 0 else machines.sumOf { it.porcentajeAvance } / machines.size
                )
                _uiState.value = UiState.Success(data)
            }
            .catch { e ->
                _uiState.value = UiState.Error(e.message ?: "Error desconocido")
            }
            .launchIn(viewModelScope)
        
        viewModelScope.launch {
            machineRepository.refreshMachines()
        }
    }
}
