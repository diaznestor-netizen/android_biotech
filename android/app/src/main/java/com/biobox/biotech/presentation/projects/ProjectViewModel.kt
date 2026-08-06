package com.biobox.biotech.presentation.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biobox.biotech.core.common.SyncStatus
import com.biobox.biotech.domain.model.Project
import com.biobox.biotech.domain.model.ProjectPriority
import com.biobox.biotech.domain.model.ProjectStatus
import com.biobox.biotech.domain.usecase.CreateProjectUseCase
import com.biobox.biotech.domain.usecase.DeleteProjectUseCase
import com.biobox.biotech.domain.usecase.ObserveProjectsUseCase
import com.biobox.biotech.domain.usecase.RefreshProjectsUseCase
import com.biobox.biotech.domain.usecase.ResolveProjectConflictUseCase
import com.biobox.biotech.domain.usecase.RetryProjectSyncUseCase
import com.biobox.biotech.domain.usecase.SyncProjectsUseCase
import com.biobox.biotech.domain.usecase.UpdateProjectUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProjectViewModel @Inject constructor(
    private val observeProjectsUseCase: ObserveProjectsUseCase,
    private val refreshProjectsUseCase: RefreshProjectsUseCase,
    private val createProjectUseCase: CreateProjectUseCase,
    private val updateProjectUseCase: UpdateProjectUseCase,
    private val deleteProjectUseCase: DeleteProjectUseCase,
    private val retryProjectSyncUseCase: RetryProjectSyncUseCase,
    private val resolveProjectConflictUseCase: ResolveProjectConflictUseCase,
    private val syncProjectsUseCase: SyncProjectsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProjectListUiState())
    val state: StateFlow<ProjectListUiState> = _state.asStateFlow()
    private var searchJob: Job? = null

    init {
        observeProjects()
        refresh()
    }

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(350)
            _state.update { current -> current.filtered() }
        }
    }

    fun onStatusFilterChange(status: ProjectStatus?) {
        _state.update { current ->
            current.copy(statusFilter = status).filtered()
        }
    }

    fun onSyncFilterChange(status: SyncStatus?) {
        _state.update { current ->
            current.copy(syncFilter = status).filtered()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, error = null) }
            refreshProjectsUseCase().fold(
                onSuccess = {
                    _state.update { it.copy(isRefreshing = false, message = "Proyectos actualizados") }
                },
                onFailure = { throwable ->
                    _state.update {
                        it.copy(
                            isRefreshing = false,
                            error = throwable.message ?: "No se pudo actualizar la información local"
                        )
                    }
                }
            )
        }
    }

    fun syncNow() {
        viewModelScope.launch {
            syncProjectsUseCase().fold(
                onSuccess = {
                    _state.update { it.copy(message = "Sincronización iniciada") }
                },
                onFailure = { throwable ->
                    _state.update { it.copy(message = throwable.message ?: "No se pudo iniciar la sincronización") }
                }
            )
        }
    }

    fun showCreateEditor() {
        _state.update {
            it.copy(
                isEditorVisible = true,
                editorMode = ProjectEditorMode.Create,
                form = ProjectFormState(localId = UUID.randomUUID().toString())
            )
        }
    }

    fun showEditEditor(project: Project) {
        _state.update {
            it.copy(
                isEditorVisible = true,
                editorMode = ProjectEditorMode.Edit,
                form = project.toFormState()
            )
        }
    }

    fun hideEditor() {
        _state.update { it.copy(isEditorVisible = false, form = ProjectFormState()) }
    }

    fun onFormCodigoChange(value: String) = updateForm { copy(codigo = value, error = null) }
    fun onFormNombreChange(value: String) = updateForm { copy(nombre = value, error = null) }
    fun onFormDescripcionChange(value: String) = updateForm { copy(descripcion = value) }
    fun onFormClienteChange(value: String) = updateForm { copy(cliente = value) }
    fun onFormResponsableChange(value: String) = updateForm { copy(responsableNombre = value) }
    fun onFormAvanceChange(value: String) = updateForm { copy(porcentajeAvance = value.filter { it.isDigit() }.take(3)) }
    fun onFormObservacionesChange(value: String) = updateForm { copy(observaciones = value) }
    fun onFormEstadoChange(value: ProjectStatus) = updateForm { copy(estado = value) }
    fun onFormPrioridadChange(value: ProjectPriority) = updateForm { copy(prioridad = value) }

    fun submitForm() {
        val current = _state.value
        val form = current.form
        val avance = form.porcentajeAvance.toIntOrNull()
        if (avance == null) {
            updateForm { copy(error = "El porcentaje de avance es inválido") }
            return
        }

        val project = Project(
            id = current.projects.firstOrNull { it.localId == form.localId }?.id,
            localId = form.localId,
            codigo = form.codigo,
            nombre = form.nombre,
            descripcion = form.descripcion.ifBlank { null },
            cliente = form.cliente.ifBlank { null },
            responsableId = null,
            responsableNombre = form.responsableNombre.ifBlank { null },
            usuarioCreadorId = current.projects.firstOrNull { it.localId == form.localId }?.usuarioCreadorId,
            estado = form.estado,
            prioridad = form.prioridad,
            porcentajeAvance = avance,
            observaciones = form.observaciones.ifBlank { null },
            version = form.version,
            syncStatus = form.syncStatus,
            organizationId = current.projects.firstOrNull { it.localId == form.localId }?.organizationId,
            tenantId = current.projects.firstOrNull { it.localId == form.localId }?.tenantId,
            conflictPayloadJson = current.projects.firstOrNull { it.localId == form.localId }?.conflictPayloadJson
        )

        viewModelScope.launch {
            updateForm { copy(isSaving = true, error = null) }
            val result = when (current.editorMode) {
                ProjectEditorMode.Create -> createProjectUseCase(project)
                ProjectEditorMode.Edit -> updateProjectUseCase(project)
            }
            result.fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            isEditorVisible = false,
                            form = ProjectFormState(),
                            message = if (current.editorMode == ProjectEditorMode.Create) {
                                "Proyecto guardado en local"
                            } else {
                                "Proyecto actualizado en local"
                            }
                        )
                    }
                },
                onFailure = { throwable ->
                    updateForm {
                        copy(
                            isSaving = false,
                            error = throwable.message ?: "No se pudo guardar el proyecto"
                        )
                    }
                }
            )
        }
    }

    fun confirmDelete(project: Project) {
        _state.update { it.copy(projectPendingDeletion = project) }
    }

    fun dismissDeleteDialog() {
        _state.update { it.copy(projectPendingDeletion = null) }
    }

    fun deleteSelectedProject() {
        val project = _state.value.projectPendingDeletion ?: return
        viewModelScope.launch {
            deleteProjectUseCase(project.localId).fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            projectPendingDeletion = null,
                            message = "Proyecto enviado a eliminación local"
                        )
                    }
                },
                onFailure = { throwable ->
                    _state.update {
                        it.copy(
                            projectPendingDeletion = null,
                            message = throwable.message ?: "No se pudo eliminar el proyecto"
                        )
                    }
                }
            )
        }
    }

    fun retrySync(project: Project) {
        viewModelScope.launch {
            retryProjectSyncUseCase(project.localId).fold(
                onSuccess = { _state.update { it.copy(message = "Reintento de sincronización programado") } },
                onFailure = { throwable -> _state.update { it.copy(message = throwable.message ?: "No se pudo reintentar") } }
            )
        }
    }

    fun askConflictResolution(project: Project) {
        _state.update { it.copy(projectPendingConflictResolution = project) }
    }

    fun dismissConflictDialog() {
        _state.update { it.copy(projectPendingConflictResolution = null) }
    }

    fun resolveConflict(useRemote: Boolean) {
        val project = _state.value.projectPendingConflictResolution ?: return
        viewModelScope.launch {
            resolveProjectConflictUseCase(project.localId, useRemote).fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            projectPendingConflictResolution = null,
                            message = if (useRemote) "Se priorizó la versión remota" else "Se reintentará la versión local"
                        )
                    }
                },
                onFailure = { throwable ->
                    _state.update {
                        it.copy(
                            projectPendingConflictResolution = null,
                            message = throwable.message ?: "No se pudo resolver el conflicto"
                        )
                    }
                }
            )
        }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }

    private fun observeProjects() {
        viewModelScope.launch {
            observeProjectsUseCase().collect { projects ->
                _state.update { current ->
                    current.copy(
                        isLoading = false,
                        projects = projects.sortedByDescending(Project::updatedAt),
                        pendingSyncCount = projects.count { it.syncStatus != SyncStatus.SYNCED }
                    ).filtered()
                }
            }
        }
    }

    private fun updateForm(transform: ProjectFormState.() -> ProjectFormState) {
        _state.update { current -> current.copy(form = current.form.transform()) }
    }

    private fun ProjectListUiState.filtered(): ProjectListUiState {
        val normalizedQuery = searchQuery.trim()
        val filtered = projects.filter { project ->
            val matchesQuery = normalizedQuery.isBlank() ||
                project.codigo.contains(normalizedQuery, ignoreCase = true) ||
                project.nombre.contains(normalizedQuery, ignoreCase = true) ||
                (project.cliente?.contains(normalizedQuery, ignoreCase = true) == true)
            val matchesStatus = statusFilter == null || project.estado == statusFilter
            val matchesSync = syncFilter == null || project.syncStatus == syncFilter
            matchesQuery && matchesStatus && matchesSync
        }
        return copy(visibleProjects = filtered)
    }

    private fun Project.toFormState(): ProjectFormState {
        return ProjectFormState(
            localId = localId,
            codigo = codigo,
            nombre = nombre,
            descripcion = descripcion.orEmpty(),
            cliente = cliente.orEmpty(),
            responsableNombre = responsableNombre.orEmpty(),
            porcentajeAvance = porcentajeAvance.toString(),
            estado = estado,
            prioridad = prioridad,
            observaciones = observaciones.orEmpty(),
            version = version,
            syncStatus = syncStatus
        )
    }
}
