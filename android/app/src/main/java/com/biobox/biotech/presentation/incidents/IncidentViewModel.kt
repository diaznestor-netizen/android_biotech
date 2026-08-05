package com.biobox.biotech.presentation.incidents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biobox.biotech.core.common.UiState
import com.biobox.biotech.domain.model.Incident
import com.biobox.biotech.domain.notifications.NotificationCenter
import com.biobox.biotech.domain.notifications.NotificationEvent
import com.biobox.biotech.domain.repository.IncidentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IncidentViewModel @Inject constructor(
    private val incidentRepository: IncidentRepository,
    private val notificationCenter: NotificationCenter
) : ViewModel() {

    private val _incidents = MutableStateFlow<UiState<List<Incident>>>(UiState.Loading)
    val incidents: StateFlow<UiState<List<Incident>>> = _incidents.asStateFlow()

    private val _currentIncident = MutableStateFlow<UiState<Incident>>(UiState.Idle)
    val currentIncident: StateFlow<UiState<Incident>> = _currentIncident.asStateFlow()

    init { loadIncidents() }

    fun loadIncidents() {
        viewModelScope.launch {
            incidentRepository.getIncidents().collect { list ->
                _incidents.value = UiState.Success(list)
            }
        }
        viewModelScope.launch { incidentRepository.refreshIncidents() }
    }

    fun loadIncident(id: Int) {
        viewModelScope.launch {
            incidentRepository.getIncidentById(id).collect { inc ->
                if (inc != null) _currentIncident.value = UiState.Success(inc)
            }
        }
    }

    fun createIncident(incident: Incident, onSuccess: () -> Unit) {
        viewModelScope.launch {
            incidentRepository.createIncident(incident)
                .onSuccess {
                    loadIncidents()
                    onSuccess()
                    
                    // Notify to Telegram
                    notificationCenter.notify(
                        NotificationEvent.IncidentReported(
                            id = incident.id.toString(),
                            machine = incident.maquinaNombre ?: "General",
                            priority = incident.gravedad.name,
                            user = incident.reportadoPor
                        )
                    )
                }
                .onFailure { _incidents.value = UiState.Error(it.message ?: "Error") }
        }
    }

    fun resolveIncident(id: Int, comentarios: String) {
        viewModelScope.launch {
            incidentRepository.resolveIncident(id, comentarios)
                .onSuccess {
                    loadIncidents()
                    
                    // Notify closure
                    notificationCenter.notify(
                        NotificationEvent.IncidentClosed(
                            id = id.toString(),
                            resolution = comentarios,
                            user = "Sistema"
                        )
                    )
                }
                .onFailure { _incidents.value = UiState.Error(it.message ?: "Error") }
        }
    }
}
