package com.biobox.biotech.domain.repository

import com.biobox.biotech.domain.model.AnalyticsData

interface AnalyticsRepository {
    suspend fun getAnalytics(): Result<AnalyticsData>
    suspend fun getDailySummary(): Result<DailySummaryData>
    suspend fun getWeeklySummary(): Result<SummaryData>
    suspend fun getMonthlySummary(): Result<SummaryData>
}

data class DailySummaryData(
    val actividadesCompletadas: Int = 0,
    val actividadesPendientes: Int = 0,
    val misionesVencidas: Int = 0,
    val incidenciasActivas: Int = 0,
    val productividad: Float = 0f,
    val horasTrabajadas: Float = 0f
)

data class SummaryData(
    val actividadesRealizadas: Int = 0,
    val maquinasCompletadas: Int = 0,
    val materialesUtilizados: Int = 0,
    val incidenciasReportadas: Int = 0,
    val misionesCompletadas: Int = 0,
    val productividadPromedio: Float = 0f
)
