package com.biobox.biotech.domain.repository

import com.biobox.biotech.core.network.ApiResult
import com.biobox.biotech.domain.model.Project
import com.biobox.biotech.domain.model.ProjectStatus
import com.biobox.biotech.domain.model.ProjectPriority
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    fun getProjects(): Flow<List<Project>>
    fun getProjectByLocalId(localId: String): Flow<Project?>
    fun getPendingSyncCount(): Flow<Int>
    
    suspend fun refreshProjects(query: String? = null): ApiResult<Unit>
    suspend fun saveProject(project: Project): Result<Unit>
    suspend fun updateProject(project: Project): Result<Unit>
    suspend fun deleteProject(localId: String): Result<Unit>

    suspend fun retrySync(localId: String): Result<Unit>
    suspend fun resolveConflict(localId: String, useRemote: Boolean): Result<Unit>
}
