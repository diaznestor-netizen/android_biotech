package com.biobox.biotech.data.remote.api

import com.biobox.biotech.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface CalendarService {
    @GET("calendar")
    suspend fun getEvents(@Query("start") startDate: Long, @Query("end") endDate: Long): Response<List<CalendarEventDto>>

    @POST("calendar")
    suspend fun createEvent(@Body request: CalendarEventRequest): Response<CalendarEventDto>

    @DELETE("calendar/{id}")
    suspend fun deleteEvent(@Path("id") id: Int): Response<Unit>
}
