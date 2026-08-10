package com.biobox.biotech.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biobox.biotech.data.remote.api.AlertService
import com.biobox.biotech.data.remote.api.IncidentAlertDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlertViewModel @Inject constructor(private val service: AlertService) : ViewModel() {
    private val _alerts = MutableStateFlow<List<IncidentAlertDto>>(emptyList())
    val alerts = _alerts.asStateFlow()

    init {
        viewModelScope.launch {
            while (isActive) {
                refresh()
                delay(15_000)
            }
        }
    }

    suspend fun refresh() {
        runCatching { service.getUnacknowledged() }.getOrNull()?.takeIf { it.isSuccessful }
            ?.body()?.let { _alerts.value = it.alerts }
    }

    fun acknowledge(id: Int) = viewModelScope.launch {
        if (runCatching { service.acknowledge(id) }.getOrNull()?.isSuccessful == true) refresh()
    }
}
