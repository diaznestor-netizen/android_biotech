package com.biobox.biotech.data.local.dao

import androidx.room.*
import com.biobox.biotech.data.local.entity.CalendarEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarEventDao {
    @Query("SELECT * FROM calendar_events WHERE fechaInicio >= :startDate AND fechaInicio <= :endDate ORDER BY fechaInicio ASC")
    fun getEvents(startDate: Long, endDate: Long): Flow<List<CalendarEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<CalendarEventEntity>)

    @Query("DELETE FROM calendar_events WHERE fechaInicio >= :startDate AND fechaInicio <= :endDate")
    suspend fun deleteEventsInRange(startDate: Long, endDate: Long)

    @Query("DELETE FROM calendar_events WHERE id = :id")
    suspend fun deleteEvent(id: Int)
}
