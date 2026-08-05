package com.biobox.biotech.presentation.machines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biobox.biotech.core.network.ApiResult
import com.biobox.biotech.domain.model.Machine
import com.biobox.biotech.domain.model.MachineStatus
import com.biobox.biotech.domain.repository.MachineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MachineListState(
    val isLoading: Boolean = false,
    val machines: List<Machine> = emptyList(),
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val searchQuery: String = "",
    val statusFilter: MachineStatus? = null
)

@HiltViewModel
class MachineViewModel @Inject constructor(
    private val machineRepository: MachineRepository
) : ViewModel() {
    private val _state = MutableStateFlow(MachineListState(isLoading = true))
    val state: StateFlow<MachineListState> = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _statusFilter = MutableStateFlow<MachineStatus?>(null)

    init {
        observeMachines()
        refresh()
    }

    private fun observeMachines() {
        combine(_searchQuery.debounce(300), _statusFilter) { query, status ->
            _state.update { it.copy(searchQuery = query, statusFilter = status) }
            Pair(query, status)
        }
        .flatMapLatest { (query, status) ->
            machineRepository.getMachines().map { list ->
                list.filter { machine ->
                    (query.isBlank() || machine.nombre.contains(query, ignoreCase = true) || machine.codigo.contains(query, ignoreCase = true)) &&
                    (status == null || machine.estado == status)
                }
            }
        }
        .onEach { machines ->
            _state.update { it.copy(machines = machines, isLoading = false) }
        }
        .launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onStatusFilterChange(status: MachineStatus?) {
        _statusFilter.value = status
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, error = null) }
            when (val result = machineRepository.refreshMachines()) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isRefreshing = false) }
                }
                is ApiResult.HttpError -> {
                    _state.update { it.copy(isRefreshing = false, error = "Error servidor: ${result.code}") }
                }
                is ApiResult.NetworkError -> {
                    _state.update { it.copy(isRefreshing = false, error = "Sin conexión a internet") }
                }
                is ApiResult.InvalidData -> {
                    _state.update { it.copy(isRefreshing = false, error = "Datos recibidos inválidos") }
                }
            }
        }
    }
}
