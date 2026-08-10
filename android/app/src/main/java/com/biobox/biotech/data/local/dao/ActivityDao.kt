package com.biobox.biotech.data.local.dao

import androidx.room.*
import com.biobox.biotech.data.local.entity.ActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities ORDER BY fecha DESC")
    fun getAllActivities(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE id = :id")
    fun getActivityById(id: Int): Flow<ActivityEntity?>

    @Query("SELECT * FROM activities WHERE id = :id")
    suspend fun getActivityByIdOnce(id: Int): ActivityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivities(activities: List<ActivityEntity>)

    @Query("DELETE FROM activities")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityEntity)

    @Query("DELETE FROM activities WHERE id = :id")
    suspend fun deleteActivity(id: Int)
}
