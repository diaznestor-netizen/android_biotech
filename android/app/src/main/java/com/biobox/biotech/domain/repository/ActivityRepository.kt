package com.biobox.biotech.domain.repository

import com.biobox.biotech.domain.model.Activity
import kotlinx.coroutines.flow.Flow

interface ActivityRepository {
    fun getActivities(): Flow<List<Activity>>
    fun getActivityById(id: Int): Flow<Activity?>
    suspend fun refreshActivities()
    suspend fun createActivity(activity: Activity): Result<Activity>
    suspend fun updateActivity(activity: Activity): Result<Activity>
    suspend fun approveActivity(id: Int): Result<Unit>
    suspend fun rejectActivity(id: Int, motivo: String): Result<Unit>
}
