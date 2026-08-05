package com.biobox.biotech.presentation.materials

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biobox.biotech.core.network.ApiResult
import com.biobox.biotech.domain.model.Material
import com.biobox.biotech.domain.notifications.NotificationCenter
import com.biobox.biotech.domain.notifications.NotificationEvent
import com.biobox.biotech.domain.repository.MaterialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MaterialListState(
    val isLoading: Boolean = false,
    val materials: List<Material> = emptyList(),
    val error: String? = null,
    val searchQuery: String = "",
    val isRefreshing: Boolean = false,
    val statusFilter: String? = null
)

@HiltViewModel
class MaterialViewModel @Inject constructor(
    private val materialRepository: MaterialRepository,
    private val notificationCenter: NotificationCenter
) : ViewModel() {
    private val _state = MutableStateFlow(MaterialListState(isLoading = true))
    val state: StateFlow<MaterialListState> = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _statusFilter = MutableStateFlow<String?>(null)

    init {
        observeMaterials()
        triggerRefresh()
    }

    private fun observeMaterials() {
        combine(_searchQuery.debounce(300), _statusFilter) { query, status ->
            _state.update { it.copy(searchQuery = query, statusFilter = status) }
            Pair(query, status)
        }
        .flatMapLatest { (query, status) ->
            materialRepository.getMaterials(query).map { list ->
                // Trigger stock alerts if critical
                list.forEach { material ->
                    if (material.cantidadDisponible <= 0) {
                        notificationCenter.notify(NotificationEvent.MaterialOut(material.nombre ?: "Desconocido"))
                    } else if (material.stockMin > 0 && material.cantidadDisponible <= material.stockMin) {
                        notificationCenter.notify(
                            NotificationEvent.StockLow(
                                item = material.nombre ?: "Desconocido",
                                currentQty = material.cantidadDisponible.toDouble(),
                                minQty = material.stockMin
                            )
                        )
                    }
                }
                
                if (status == null) list
                else list.filter { it.estado == status }
            }
        }
        .onEach { materials ->
            _state.update { it.copy(materials = materials, isLoading = false) }
        }
        .launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onStatusFilterChange(status: String?) {
        _statusFilter.value = status
    }

    fun refresh() {
        triggerRefresh(_searchQuery.value)
    }

    private fun triggerRefresh(query: String? = null) {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, error = null) }
            when (val result = materialRepository.refreshMaterials(query)) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isRefreshing = false) }
                }
                is ApiResult.HttpError -> {
                    _state.update { it.copy(isRefreshing = false, error = "Error servidor: ${result.code}") }
                }
                is ApiResult.NetworkError -> {
                    _state.update { it.copy(isRefreshing = false, error = "Sin conexión a internet") }
                }
                else -> {
                    _state.update { it.copy(isRefreshing = false) }
                }
            }
        }
    }
}
