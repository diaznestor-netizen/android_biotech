package com.biobox.biotech.presentation.projects

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biobox.biotech.core.common.SyncStatus
import com.biobox.biotech.domain.model.Project
import com.biobox.biotech.domain.usecase.DeleteProjectUseCase
import com.biobox.biotech.domain.usecase.GetProjectByLocalIdUseCase
import com.biobox.biotech.domain.usecase.RefreshProjectsUseCase
import com.biobox.biotech.domain.usecase.ResolveProjectConflictUseCase
import com.biobox.biotech.domain.usecase.RetryProjectSyncUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProjectDetailUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val project: Project? = null,
    val error: String? = null,
    val message: String? = null,
    val showDeleteDialog: Boolean = false,
    val showConflictDialog: Boolean = false
)

sealed interface ProjectDetailEvent {
    data object NavigateBack : ProjectDetailEvent
    data class NavigateToEdit(val localId: String) : ProjectDetailEvent
    data class NavigateToConflictMerge(val localId: String) : ProjectDetailEvent
}

@HiltViewModel
class ProjectDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getProjectByLocalIdUseCase: GetProjectByLocalIdUseCase,
    private val refreshProjectsUseCase: RefreshProjectsUseCase,
    private val deleteProjectUseCase: DeleteProjectUseCase,
    private val retryProjectSyncUseCase: RetryProjectSyncUseCase,
    private val resolveProjectConflictUseCase: ResolveProjectConflictUseCase
) : ViewModel() {

    private val localId: String = savedStateHandle.get<String>("localId").orEmpty()

    private val _state = MutableStateFlow(ProjectDetailUiState())
    val state: StateFlow<ProjectDetailUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ProjectDetailEvent>()
    val events: SharedFlow<ProjectDetailEvent> = _events.asSharedFlow()

    init {
        if (localId.isBlank()) {
            _state.update { it.copy(isLoading = false, error = "Identificador de proyecto inválido") }
        } else {
            observeProject()
            refresh()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            refreshProjectsUseCase().onFailure {
                _state.update { current -> current.copy(message = it.message ?: "No se pudo actualizar") }
            }
            _state.update { it.copy(isRefreshing = false) }
        }
    }

    fun editProject() {
        val project = _state.value.project ?: return
        viewModelScope.launch {
            _events.emit(ProjectDetailEvent.NavigateToEdit(project.localId))
        }
    }

    fun askDelete() {
        _state.update { it.copy(showDeleteDialog = true) }
    }

    fun dismissDelete() {
        _state.update { it.copy(showDeleteDialog = false) }
    }

    fun deleteProject() {
        val project = _state.value.project ?: return
        viewModelScope.launch {
            deleteProjectUseCase(project.localId).fold(
                onSuccess = {
                    _state.update { it.copy(showDeleteDialog = false, message = "Eliminación local confirmada") }
                    _events.emit(ProjectDetailEvent.NavigateBack)
                },
                onFailure = { throwable ->
                    _state.update { it.copy(showDeleteDialog = false, message = throwable.message ?: "No se pudo eliminar") }
                }
            )
        }
    }

    fun retrySync() {
        val project = _state.value.project ?: return
        viewModelScope.launch {
            retryProjectSyncUseCase(project.localId).fold(
                onSuccess = { _state.update { it.copy(message = "Reintento programado") } },
                onFailure = { throwable -> _state.update { it.copy(message = throwable.message ?: "No se pudo reintentar") } }
            )
        }
    }

    fun askConflictResolution() {
        _state.update { it.copy(showConflictDialog = true) }
    }

    fun dismissConflictResolution() {
        _state.update { it.copy(showConflictDialog = false) }
    }

    fun resolveConflict(useRemote: Boolean) {
        val project = _state.value.project ?: return
        viewModelScope.launch {
            resolveProjectConflictUseCase(project.localId, useRemote).fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            showConflictDialog = false,
                            message = if (useRemote) "Se aceptó la versión remota" else "Se reintentará la versión local"
                        )
                    }
                },
                onFailure = { throwable ->
                    _state.update { it.copy(showConflictDialog = false, message = throwable.message ?: "No se pudo resolver") }
                }
            )
        }
    }

    fun combineConflictManually() {
        val project = _state.value.project ?: return
        viewModelScope.launch {
            _state.update { it.copy(showConflictDialog = false) }
            _events.emit(ProjectDetailEvent.NavigateToConflictMerge(project.localId))
        }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }

    private fun observeProject() {
        viewModelScope.launch {
            getProjectByLocalIdUseCase(localId).collect { project ->
                _state.update {
                    when {
                        project == null && it.project != null -> it.copy(
                            isLoading = false,
                            project = null,
                            error = "El proyecto dejó de estar disponible en la base local"
                        )
                        project == null -> it.copy(isLoading = false, project = null, error = "Proyecto no encontrado")
                        else -> it.copy(isLoading = false, project = project, error = null)
                    }
                }
            }
        }
    }
}
