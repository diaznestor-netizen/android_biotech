package com.biobox.biotech.presentation.projects

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biobox.biotech.domain.model.Project
import com.biobox.biotech.domain.model.ProjectPriority
import com.biobox.biotech.domain.model.ProjectStatus
import com.biobox.biotech.domain.notifications.NotificationCenter
import com.biobox.biotech.domain.notifications.NotificationEvent
import com.biobox.biotech.domain.usecase.CreateProjectUseCase
import com.biobox.biotech.domain.usecase.GetProjectByLocalIdUseCase
import com.biobox.biotech.domain.usecase.ObserveProjectsUseCase
import com.biobox.biotech.domain.usecase.UpdateProjectUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProjectFormScreenState(
    val isLoading: Boolean = true,
    val isEditMode: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val form: ProjectFormState = ProjectFormState(localId = UUID.randomUUID().toString()),
    val conflict: ProjectFormConflictUiState = ProjectFormConflictUiState(),
    val error: String? = null,
    val message: String? = null
)

sealed interface ProjectFormEvent {
    data object NavigateBack : ProjectFormEvent
}

@HiltViewModel
class ProjectFormViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getProjectByLocalIdUseCase: GetProjectByLocalIdUseCase,
    private val observeProjectsUseCase: ObserveProjectsUseCase,
    private val createProjectUseCase: CreateProjectUseCase,
    private val updateProjectUseCase: UpdateProjectUseCase,
    private val notificationCenter: NotificationCenter
) : ViewModel() {

    private val localId: String = savedStateHandle.get<String>("localId").orEmpty()
    private val mergeConflict: Boolean = savedStateHandle["mergeConflict"] ?: false
    private val _state = MutableStateFlow(ProjectFormScreenState(isLoading = localId.isNotBlank()))
    val state: StateFlow<ProjectFormScreenState> = _state.asStateFlow()
    private val existingProjects = MutableStateFlow<List<Project>>(emptyList())

    private val _events = MutableSharedFlow<ProjectFormEvent>()
    val events: SharedFlow<ProjectFormEvent> = _events.asSharedFlow()

    init {
        observeProjects()
        if (localId.isBlank()) {
            _state.update { current ->
                current.copy(
                    isLoading = false,
                    form = restoreDraft(current.form)
                )
            }
        } else {
            viewModelScope.launch {
                getProjectByLocalIdUseCase(localId).collect { project ->
                    if (project == null) {
                        _state.update { it.copy(isLoading = false, error = "Proyecto no encontrado") }
                    } else {
                        val baseForm = restoreDraft(project.toFormState())
                        val conflictDetails = ProjectConflictComparator.parse(
                            project.conflictPayloadJson,
                            project.toPresentationSnapshot()
                        )
                        _state.update {
                            it.copy(
                                isLoading = false,
                                isEditMode = true,
                                form = baseForm,
                                conflict = ProjectFormConflictUiState(
                                    isMergeMode = mergeConflict && project.syncStatus == com.biobox.biotech.core.common.SyncStatus.CONFLICT && conflictDetails != null,
                                    remoteVersion = conflictDetails?.remoteVersion,
                                    differences = conflictDetails?.differences.orEmpty(),
                                    remoteForm = conflictDetails?.toRemoteFormState(project.localId)
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    fun onCodigoChange(value: String) = updateForm { copy(codigo = value, error = null) }
    fun onNombreChange(value: String) = updateForm { copy(nombre = value, error = null) }
    fun onDescripcionChange(value: String) = updateForm { copy(descripcion = value) }
    fun onClienteChange(value: String) = updateForm { copy(cliente = value) }
    fun onResponsableChange(value: String) = updateForm { copy(responsableNombre = value) }
    fun onFechaInicioChange(value: Long?) = updateForm { copy(fechaInicio = value) }
    fun onFechaFinEstimadaChange(value: Long?) = updateForm { copy(fechaFinEstimada = value) }
    fun onFechaFinRealChange(value: Long?) = updateForm { copy(fechaFinReal = value) }
    fun onAvanceChange(value: String) = updateForm { copy(porcentajeAvance = value.filter { it.isDigit() }.take(3)) }
    fun onObservacionesChange(value: String) = updateForm { copy(observaciones = value) }
    fun onEstadoChange(value: ProjectStatus) = updateForm { copy(estado = value) }
    fun onPrioridadChange(value: ProjectPriority) = updateForm { copy(prioridad = value) }
    fun useRemoteValue(field: String) {
        val remote = _state.value.conflict.remoteForm ?: return
        when (field) {
            "Código" -> onCodigoChange(remote.codigo)
            "Nombre" -> onNombreChange(remote.nombre)
            "Descripción" -> onDescripcionChange(remote.descripcion)
            "Cliente" -> onClienteChange(remote.cliente)
            "Responsable" -> onResponsableChange(remote.responsableNombre)
            "Estado" -> onEstadoChange(remote.estado)
            "Prioridad" -> onPrioridadChange(remote.prioridad)
            "Fecha inicio" -> onFechaInicioChange(remote.fechaInicio)
            "Fecha fin estimada" -> onFechaFinEstimadaChange(remote.fechaFinEstimada)
            "Fecha fin real" -> onFechaFinRealChange(remote.fechaFinReal)
            "Avance" -> onAvanceChange(remote.porcentajeAvance)
            "Observaciones" -> onObservacionesChange(remote.observaciones)
        }
    }

    fun useLocalValue(field: String) {
        val original = existingProjects.value.firstOrNull { it.localId == localId }?.toFormState() ?: return
        when (field) {
            "Código" -> onCodigoChange(original.codigo)
            "Nombre" -> onNombreChange(original.nombre)
            "Descripción" -> onDescripcionChange(original.descripcion)
            "Cliente" -> onClienteChange(original.cliente)
            "Responsable" -> onResponsableChange(original.responsableNombre)
            "Estado" -> onEstadoChange(original.estado)
            "Prioridad" -> onPrioridadChange(original.prioridad)
            "Fecha inicio" -> onFechaInicioChange(original.fechaInicio)
            "Fecha fin estimada" -> onFechaFinEstimadaChange(original.fechaFinEstimada)
            "Fecha fin real" -> onFechaFinRealChange(original.fechaFinReal)
            "Avance" -> onAvanceChange(original.porcentajeAvance)
            "Observaciones" -> onObservacionesChange(original.observaciones)
        }
    }

    fun save() {
        val current = _state.value
        val form = current.form
        val fieldErrors = validateForm(form)
        if (fieldErrors.isNotEmpty()) {
            updateForm { copy(error = "Corrige los campos marcados", fieldErrors = fieldErrors) }
            return
        }
        if (form.isSaving) return
        updateForm { copy(isSaving = true, error = null) }
        val progress = form.porcentajeAvance.toInt()

        val project = Project(
            id = null,
            localId = form.localId,
            codigo = form.codigo,
            nombre = form.nombre,
            descripcion = form.descripcion.ifBlank { null },
            cliente = form.cliente.ifBlank { null },
            responsableId = null,
            responsableNombre = form.responsableNombre.ifBlank { null },
            fechaInicio = form.fechaInicio,
            fechaFinEstimada = form.fechaFinEstimada,
            fechaFinReal = form.fechaFinReal,
            estado = form.estado,
            prioridad = form.prioridad,
            porcentajeAvance = progress,
            observaciones = form.observaciones.ifBlank { null },
            version = form.version,
            syncStatus = form.syncStatus
        )

        viewModelScope.launch {
            val result = if (current.isEditMode) updateProjectUseCase(project) else createProjectUseCase(project)
            result.fold(
                onSuccess = {
                    _state.update { it.copy(message = "Guardado local confirmado", hasUnsavedChanges = false) }
                    
                    // Notify project creation
                    if (!current.isEditMode) {
                        notificationCenter.notify(
                            NotificationEvent.ProjectCreated(
                                id = project.codigo,
                                name = project.nombre,
                                manager = project.responsableNombre ?: "Sin asignar"
                            )
                        )
                    } else if (project.prioridad == ProjectPriority.CRITICA) {
                        notificationCenter.notify(
                            NotificationEvent.ProjectPriorityChanged(
                                id = project.codigo,
                                name = project.nombre,
                                newPriority = "CRÍTICA"
                            )
                        )
                    }

                    _events.emit(ProjectFormEvent.NavigateBack)
                },
                onFailure = { throwable ->
                    updateForm { copy(isSaving = false, error = throwable.message ?: "No se pudo guardar") }
                }
            )
        }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }

    private fun updateForm(transform: ProjectFormState.() -> ProjectFormState) {
        _state.update { current ->
            val updatedForm = current.form.transform()
            persistDraft(updatedForm)
            current.copy(form = updatedForm, hasUnsavedChanges = true)
        }
    }

    private fun validateForm(form: ProjectFormState): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        val progress = form.porcentajeAvance.toIntOrNull()
        if (form.codigo.isBlank()) errors["codigo"] = "El código es obligatorio"
        if (form.nombre.isBlank()) errors["nombre"] = "El nombre es obligatorio"
        val normalizedFormCode = com.biobox.biotech.domain.usecase.ProjectValidation.normalizeCode(form.codigo)
        if (existingProjects.value.any { it.localId != form.localId && com.biobox.biotech.domain.usecase.ProjectValidation.normalizeCode(it.codigo) == normalizedFormCode }) {
            errors["codigo"] = "Ya existe un proyecto con este código en la organización actual"
        }
        if (progress == null || progress !in 0..100) errors["porcentajeAvance"] = "El porcentaje debe estar entre 0 y 100"
        if (form.fechaInicio != null && form.fechaFinEstimada != null && form.fechaFinEstimada < form.fechaInicio) {
            errors["fechaFinEstimada"] = "La fecha estimada no puede ser anterior a la fecha inicial"
        }
        if (form.fechaInicio != null && form.fechaFinReal != null && form.fechaFinReal < form.fechaInicio) {
            errors["fechaFinReal"] = "La fecha real no puede ser anterior a la fecha inicial"
        }
        if (form.estado == ProjectStatus.FINALIZADO && form.fechaFinReal == null) {
            errors["estado"] = "Un proyecto finalizado debe tener fecha real de finalización"
        }
        return errors
    }

    private fun observeProjects() {
        viewModelScope.launch {
            observeProjectsUseCase().collect { projects ->
                existingProjects.value = projects
            }
        }
    }

    private fun persistDraft(form: ProjectFormState) {
        savedStateHandle["draft_localId"] = form.localId
        savedStateHandle["draft_codigo"] = form.codigo
        savedStateHandle["draft_nombre"] = form.nombre
        savedStateHandle["draft_descripcion"] = form.descripcion
        savedStateHandle["draft_cliente"] = form.cliente
        savedStateHandle["draft_responsableNombre"] = form.responsableNombre
        savedStateHandle["draft_fechaInicio"] = form.fechaInicio
        savedStateHandle["draft_fechaFinEstimada"] = form.fechaFinEstimada
        savedStateHandle["draft_fechaFinReal"] = form.fechaFinReal
        savedStateHandle["draft_porcentajeAvance"] = form.porcentajeAvance
        savedStateHandle["draft_estado"] = form.estado.name
        savedStateHandle["draft_prioridad"] = form.prioridad.name
        savedStateHandle["draft_observaciones"] = form.observaciones
        savedStateHandle["draft_version"] = form.version
    }

    private fun restoreDraft(defaultForm: ProjectFormState): ProjectFormState {
        val draftCodigo = savedStateHandle.get<String>("draft_codigo")
        if (draftCodigo == null && localId.isBlank()) return defaultForm
        return defaultForm.copy(
            localId = savedStateHandle["draft_localId"] ?: defaultForm.localId,
            codigo = draftCodigo ?: defaultForm.codigo,
            nombre = savedStateHandle["draft_nombre"] ?: defaultForm.nombre,
            descripcion = savedStateHandle["draft_descripcion"] ?: defaultForm.descripcion,
            cliente = savedStateHandle["draft_cliente"] ?: defaultForm.cliente,
            responsableNombre = savedStateHandle["draft_responsableNombre"] ?: defaultForm.responsableNombre,
            fechaInicio = savedStateHandle["draft_fechaInicio"] ?: defaultForm.fechaInicio,
            fechaFinEstimada = savedStateHandle["draft_fechaFinEstimada"] ?: defaultForm.fechaFinEstimada,
            fechaFinReal = savedStateHandle["draft_fechaFinReal"] ?: defaultForm.fechaFinReal,
            porcentajeAvance = savedStateHandle["draft_porcentajeAvance"] ?: defaultForm.porcentajeAvance,
            estado = savedStateHandle.get<String>("draft_estado")?.let { value ->
                ProjectStatus.entries.firstOrNull { it.name == value }
            } ?: defaultForm.estado,
            prioridad = savedStateHandle.get<String>("draft_prioridad")?.let { value ->
                ProjectPriority.entries.firstOrNull { it.name == value }
            } ?: defaultForm.prioridad,
            observaciones = savedStateHandle["draft_observaciones"] ?: defaultForm.observaciones,
            version = savedStateHandle["draft_version"] ?: defaultForm.version
        )
    }

    private fun Project.toFormState(): ProjectFormState {
        return ProjectFormState(
            localId = localId,
            codigo = codigo,
            nombre = nombre,
            descripcion = descripcion.orEmpty(),
            cliente = cliente.orEmpty(),
            responsableNombre = responsableNombre.orEmpty(),
            fechaInicio = fechaInicio,
            fechaFinEstimada = fechaFinEstimada,
            fechaFinReal = fechaFinReal,
            porcentajeAvance = porcentajeAvance.toString(),
            estado = estado,
            prioridad = prioridad,
            observaciones = observaciones.orEmpty(),
            version = version,
            syncStatus = syncStatus
        )
    }

    private fun Project.toPresentationSnapshot(): ProjectPresentationSnapshot {
        return ProjectPresentationSnapshot(
            codigo = codigo,
            nombre = nombre,
            descripcion = descripcion.orEmpty(),
            cliente = cliente.orEmpty(),
            responsable = responsableNombre.orEmpty(),
            estado = estado.name,
            prioridad = prioridad.name,
            fechaInicio = formatProjectDate(fechaInicio),
            fechaFinEstimada = formatProjectDate(fechaFinEstimada),
            fechaFinReal = formatProjectDate(fechaFinReal),
            porcentajeAvance = porcentajeAvance.toString(),
            observaciones = observaciones.orEmpty()
        )
    }
}
