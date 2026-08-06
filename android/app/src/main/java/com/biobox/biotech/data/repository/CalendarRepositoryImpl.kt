package com.biobox.biotech.data.repository

import com.biobox.biotech.data.local.dao.CalendarEventDao
import com.biobox.biotech.data.mapper.toDomain
import com.biobox.biotech.data.mapper.toEntity
import com.biobox.biotech.data.remote.api.CalendarService
import com.biobox.biotech.data.remote.dto.CalendarEventRequest
import com.biobox.biotech.domain.model.CalendarEvent
import com.biobox.biotech.domain.repository.CalendarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CalendarRepositoryImpl @Inject constructor(
    private val calendarService: CalendarService,
    private val calendarEventDao: CalendarEventDao
) : CalendarRepository {

    override fun getEvents(startDate: Long, endDate: Long): Flow<List<CalendarEvent>> {
        return calendarEventDao.getEvents(startDate, endDate).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun refreshEvents(startDate: Long, endDate: Long) {
        try {
            val response = calendarService.getEvents(startDate, endDate)
            if (response.isSuccessful) {
                calendarEventDao.deleteEventsInRange(startDate, endDate)
                val events = response.body().orEmpty().map { it.toEntity() }
                calendarEventDao.insertEvents(events)
            }
        } catch (_: Exception) { }
    }

    override suspend fun createEvent(event: CalendarEvent): Result<CalendarEvent> = runCatching {
        val request = CalendarEventRequest(
            titulo = event.titulo,
            descripcion = event.descripcion,
            tipo = event.tipo.name,
            fechaInicio = event.fechaInicio,
            fechaFin = event.fechaFin,
            todoElDia = event.todoElDia,
            maquinaId = event.maquinaId,
            proyecto = event.proyecto
        )
        val response = calendarService.createEvent(request)
        if (response.isSuccessful) {
            val dto = response.body() ?: throw Exception("Respuesta vacía")
            dto.toEntity().toDomain()
        } else throw Exception("Error al crear evento: ${response.code()}")
    }

    override suspend fun deleteEvent(id: Int): Result<Unit> = runCatching {
        val response = calendarService.deleteEvent(id)
        if (!response.isSuccessful) throw Exception("Error al eliminar: ${response.code()}")
        calendarEventDao.deleteEvent(id)
    }
}
