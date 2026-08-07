package com.biobox.biotech.presentation.machines

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biobox.biotech.core.network.ApiResult
import com.biobox.biotech.domain.model.Component
import com.biobox.biotech.domain.model.Machine
import com.biobox.biotech.domain.repository.CompletionCheck
import com.biobox.biotech.domain.repository.MachineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MachineDetailState(
    val isLoading: Boolean = false,
    val machine: Machine? = null,
    val components: List<Component> = emptyList(),
    val completion: CompletionCheck? = null,
    val error: String? = null,
    val saving: Boolean = false
)

@HiltViewModel
class MachineDetailViewModel @Inject constructor(
    private val machineRepository: MachineRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val machineId: Int = savedStateHandle.get<Int>("id") ?: -1
    private val _state = MutableStateFlow(MachineDetailState(isLoading = true))
    val state: StateFlow<MachineDetailState> = _state.asStateFlow()

    init {
        if (machineId <= 0) {
            _state.update { it.copy(isLoading = false, error = "Identificador de máquina no válido") }
        } else {
            machineRepository.getMachineById(machineId)
                .onEach { machine -> _state.update { it.copy(machine = machine) } }
                .launchIn(viewModelScope)
            loadMachineProduction()
        }
    }

    fun loadMachineProduction() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = machineRepository.refreshMachineProduction(machineId)) {
                is ApiResult.Success -> _state.update {
                    it.copy(isLoading = false, machine = result.data.machine, components = result.data.components, completion = result.data.completion)
                }
                is ApiResult.HttpError -> _state.update { it.copy(isLoading = false, error = "Error servidor: " + result.code) }
                is ApiResult.NetworkError -> _state.update { it.copy(isLoading = false, error = "Sin conexión a internet") }
                is ApiResult.InvalidData -> _state.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }

    fun updateComponent(componentId: Int, state: String) {
        viewModelScope.launch {
            _state.update { it.copy(saving = true, error = null) }
            when (val result = machineRepository.updateComponent(machineId, componentId, state)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(saving = false, completion = result.data) }
                    loadMachineProduction()
                }
                is ApiResult.HttpError -> _state.update { it.copy(saving = false, error = "No se pudo actualizar (" + result.code + ")") }
                is ApiResult.NetworkError -> _state.update { it.copy(saving = false, error = "Sin conexión a internet") }
                is ApiResult.InvalidData -> _state.update { it.copy(saving = false, error = result.message) }
            }
        }
    }

    fun transitionState(state: String) {
        viewModelScope.launch {
            _state.update { it.copy(saving = true, error = null) }
            when (val result = machineRepository.transitionState(machineId, state)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(saving = false) }
                    loadMachineProduction()
                }
                is ApiResult.HttpError -> _state.update { it.copy(saving = false, error = "No se pudo cambiar el estado (" + result.code + ")") }
                is ApiResult.NetworkError -> _state.update { it.copy(saving = false, error = "Sin conexión a internet") }
                is ApiResult.InvalidData -> _state.update { it.copy(saving = false, error = result.message) }
            }
        }
    }
}