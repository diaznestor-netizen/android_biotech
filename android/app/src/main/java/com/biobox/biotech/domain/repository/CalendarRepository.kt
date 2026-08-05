package com.biobox.biotech.domain.repository

import com.biobox.biotech.domain.model.CalendarEvent
import kotlinx.coroutines.flow.Flow

interface CalendarRepository {
    fun getEvents(startDate: Long, endDate: Long): Flow<List<CalendarEvent>>
    suspend fun refreshEvents(startDate: Long, endDate: Long)
    suspend fun createEvent(event: CalendarEvent): Result<CalendarEvent>
    suspend fun deleteEvent(id: Int): Result<Unit>
}
