package com.biobox.biotech.data.remote.api

import com.biobox.biotech.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.GET

interface AnalyticsService {
    @GET("analytics")
    suspend fun getAnalytics(): Response<AnalyticsDto>

    @GET("analytics/daily")
    suspend fun getDailySummary(): Response<DailySummaryDto>

    @GET("analytics/weekly")
    suspend fun getWeeklySummary(): Response<SummaryDataDto>

    @GET("analytics/monthly")
    suspend fun getMonthlySummary(): Response<SummaryDataDto>
}
