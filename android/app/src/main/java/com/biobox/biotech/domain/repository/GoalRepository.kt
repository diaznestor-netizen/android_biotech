package com.biobox.biotech.domain.repository

import com.biobox.biotech.domain.model.Goal
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun getGoals(): Flow<List<Goal>>
    fun getGoalById(id: Int): Flow<Goal?>
    suspend fun refreshGoals()
    suspend fun createGoal(goal: Goal): Result<Goal>
    suspend fun updateGoal(goal: Goal): Result<Goal>
    suspend fun deleteGoal(id: Int): Result<Unit>
}
