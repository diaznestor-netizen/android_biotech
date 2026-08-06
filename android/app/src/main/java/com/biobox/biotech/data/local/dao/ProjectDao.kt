package com.biobox.biotech.data.local.dao

import androidx.room.*
import com.biobox.biotech.core.common.SyncStatus
import com.biobox.biotech.data.local.entity.ProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects WHERE deletedAt IS NULL ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE localId = :localId")
    suspend fun getProjectByLocalId(localId: String): ProjectEntity?

    @Query("SELECT * FROM projects WHERE localId = :localId")
    fun observeProjectByLocalId(localId: String): Flow<ProjectEntity?>

    @Query("SELECT * FROM projects WHERE remoteId = :remoteId")
    suspend fun getProjectByRemoteId(remoteId: Int): ProjectEntity?

    @Query("SELECT * FROM projects WHERE codigo = :codigo AND deletedAt IS NULL")
    suspend fun getProjectByCodigo(codigo: String): ProjectEntity?

    @Query("SELECT * FROM projects WHERE estado = :estado AND deletedAt IS NULL")
    fun getProjectsByEstado(estado: String): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE syncStatus != 'SYNCED'")
    fun getPendingSincronization(): Flow<List<ProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertProjectInternal(project: ProjectEntity)

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Update
    suspend fun updateProjects(projects: List<ProjectEntity>)

    @Query("UPDATE projects SET deletedAt = :timestamp WHERE localId = :localId")
    suspend fun softDelete(localId: String, timestamp: Long): Int

    @Query("""
        UPDATE projects
        SET syncStatus = :status,
            conflictPayloadJson = :conflictPayloadJson,
            updatedAt = :updatedAt
        WHERE localId = :localId
    """)
    suspend fun updateSyncMetadata(
        localId: String,
        status: SyncStatus,
        conflictPayloadJson: String?,
        updatedAt: Long
    ): Int

    @Query("""
        UPDATE projects
        SET remoteId = :remoteId,
            version = :version,
            syncStatus = :status,
            conflictPayloadJson = NULL,
            deletedAt = NULL,
            updatedAt = :updatedAt
        WHERE localId = :localId
    """)
    suspend fun markAsSynced(
        localId: String,
        remoteId: Int,
        version: Int,
        status: SyncStatus,
        updatedAt: Long
    ): Int

    @Query("DELETE FROM projects")
    suspend fun deleteAll()

    @Transaction
    suspend fun insertProject(project: ProjectEntity) {
        val existing = getProjectByLocalId(project.localId)
        if (existing == null) {
            insertProjectInternal(project)
        } else {
            updateProject(project)
        }
    }

    @Transaction
    suspend fun insertProjects(projects: List<ProjectEntity>) {
        projects.forEach { insertProject(it) }
    }

    @Transaction
    suspend fun updateSyncStatus(localId: String, remoteId: Int, version: Int, status: SyncStatus) {
        val existing = getProjectByLocalId(localId)
        if (existing != null) {
            updateProject(existing.copy(
                remoteId = remoteId,
                version = version,
                syncStatus = status,
                conflictPayloadJson = null,
                updatedAt = System.currentTimeMillis()
            ))
        }
    }
}
