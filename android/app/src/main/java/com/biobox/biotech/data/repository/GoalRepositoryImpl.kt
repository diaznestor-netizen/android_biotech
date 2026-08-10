package com.biobox.biotech.data.repository

import com.biobox.biotech.data.local.dao.GoalDao
import com.biobox.biotech.data.local.dao.SyncOperationDao
import com.biobox.biotech.data.local.entity.SyncOperationEntity
import com.biobox.biotech.data.local.entity.SyncOperationStatus
import com.biobox.biotech.core.common.SyncStatus
import com.biobox.biotech.data.mapper.toDomain
import com.biobox.biotech.data.mapper.toEntity
import com.biobox.biotech.data.remote.api.GoalService
import com.biobox.biotech.domain.model.Goal
import com.biobox.biotech.domain.repository.GoalRepository
import com.biobox.biotech.domain.sync.GlobalSyncManager
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class GoalRepositoryImpl @Inject constructor(
    private val goalService: GoalService,
    private val goalDao: GoalDao,
    private val syncOperationDao: SyncOperationDao,
    private val globalSyncManager: GlobalSyncManager
) : GoalRepository {

    private val gson = Gson()

    override fun getGoals(): Flow<List<Goal>> {
        return goalDao.getAllGoals().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getGoalById(id: Int): Flow<Goal?> {
        return goalDao.getGoalById(id).map { it?.toDomain() }
    }

    override suspend fun refreshGoals() {
        try {
            val response = goalService.getGoals()
            if (response.isSuccessful) {
                val goals = response.body().orEmpty().map { it.toEntity() }
                goalDao.insertGoals(goals)
            }
        } catch (_: Exception) { }
    }

    override suspend fun createGoal(goal: Goal): Result<Goal> = runCatching {
        val entity = goal.toEntity(SyncStatus.PENDING)
        goalDao.insertGoal(entity)
        
        syncOperationDao.insertOperation(SyncOperationEntity(
            id = UUID.randomUUID().toString(),
            entityType = "GOAL",
            entityLocalId = goal.id.toString(),
            operation = "CREATE",
            payloadJson = gson.toJson(goal),
            status = SyncOperationStatus.PENDING
        ))
        globalSyncManager.enqueueSync()
        goal
    }

    override suspend fun updateGoal(goal: Goal): Result<Goal> = runCatching {
        val entity = goal.toEntity(SyncStatus.PENDING)
        goalDao.insertGoal(entity)
        
        syncOperationDao.insertOperation(SyncOperationEntity(
            id = UUID.randomUUID().toString(),
            entityType = "GOAL",
            entityLocalId = goal.id.toString(),
            operation = "UPDATE",
            payloadJson = gson.toJson(goal),
            status = SyncOperationStatus.PENDING
        ))
        globalSyncManager.enqueueSync()
        goal
    }

    override suspend fun deleteGoal(id: Int): Result<Unit> = runCatching {
        // En este caso, el objeto Goal para el payload podría no estar en el DAO si se borra antes.
        // Pero el SyncOperation guardará el estado PENDING.
        // Para DELETE, solemos guardar una op con el ID.
        syncOperationDao.insertOperation(SyncOperationEntity(
            id = UUID.randomUUID().toString(),
            entityType = "GOAL",
            entityLocalId = id.toString(),
            operation = "DELETE",
            payloadJson = "{\"id\":$id}",
            status = SyncOperationStatus.PENDING
        ))
        globalSyncManager.enqueueSync()
    }
}
