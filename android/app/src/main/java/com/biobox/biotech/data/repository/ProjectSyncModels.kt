package com.biobox.biotech.data.repository

import com.biobox.biotech.data.remote.dto.ProjectDto

internal data class ProjectAccountContext(
    val userId: String?,
    val organizationId: String?,
    val tenantId: String?
)

internal data class SyncConflictPayload(
    val localVersion: Int,
    val operation: String,
    val serverProject: ProjectDto? = null,
    val message: String? = null
)

internal sealed interface SyncExecution {
    data object Success : SyncExecution
    data object Retry : SyncExecution
    data class PermanentError(val message: String) : SyncExecution
    data class Conflict(val payload: SyncConflictPayload) : SyncExecution
}
