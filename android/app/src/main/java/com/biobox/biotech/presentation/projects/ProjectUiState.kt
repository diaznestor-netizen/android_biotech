package com.biobox.biotech.presentation.projects

import com.biobox.biotech.core.common.SyncStatus
import com.biobox.biotech.domain.model.Project
import com.biobox.biotech.domain.model.ProjectPriority
import com.biobox.biotech.domain.model.ProjectStatus

data class ProjectListUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val projects: List<Project> = emptyList(),
    val visibleProjects: List<Project> = emptyList(),
    val searchQuery: String = "",
    val statusFilter: ProjectStatus? = null,
    val syncFilter: SyncStatus? = null,
    val error: String? = null,
    val message: String? = null,
    val pendingSyncCount: Int = 0,
    val isEditorVisible: Boolean = false,
    val editorMode: ProjectEditorMode = ProjectEditorMode.Create,
    val form: ProjectFormState = ProjectFormState(),
    val projectPendingDeletion: Project? = null,
    val projectPendingConflictResolution: Project? = null
)

enum class ProjectEditorMode {
    Create,
    Edit
}

data class ProjectFormState(
    val localId: String = "",
    val codigo: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val cliente: String = "",
    val responsableNombre: String = "",
    val fechaInicio: Long? = null,
    val fechaFinEstimada: Long? = null,
    val fechaFinReal: Long? = null,
    val porcentajeAvance: String = "0",
    val estado: ProjectStatus = ProjectStatus.PLANEADO,
    val prioridad: ProjectPriority = ProjectPriority.MEDIA,
    val observaciones: String = "",
    val version: Int = 0,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val error: String? = null,
    val fieldErrors: Map<String, String> = emptyMap(),
    val isSaving: Boolean = false
)

data class ProjectFormConflictUiState(
    val isMergeMode: Boolean = false,
    val remoteVersion: Int? = null,
    val differences: List<ProjectFieldDifference> = emptyList(),
    val remoteForm: ProjectFormState? = null
)
