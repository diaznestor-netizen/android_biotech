package com.biobox.biotech.data.repository

import com.biobox.biotech.data.remote.api.AnalyticsService
import com.biobox.biotech.domain.model.*
import com.biobox.biotech.domain.repository.AnalyticsRepository
import com.biobox.biotech.domain.repository.DailySummaryData
import com.biobox.biotech.domain.repository.SummaryData
import javax.inject.Inject

class AnalyticsRepositoryImpl @Inject constructor(
    private val analyticsService: AnalyticsService
) : AnalyticsRepository {

    override suspend fun getAnalytics(): Result<AnalyticsData> = runCatching {
        val response = analyticsService.getAnalytics()
        if (response.isSuccessful) {
            val dto = response.body() ?: throw Exception("Respuesta vacía")
            AnalyticsData(
                avancePorProyecto = dto.avancePorProyecto.map { ProgressItem(it.nombre, it.porcentaje, it.meta) },
                avancePorMaquina = dto.avancePorMaquina.map { ProgressItem(it.nombre, it.porcentaje, it.meta) },
                productividadPorEmpleado = dto.productividadPorEmpleado.map {
                    ProductivityItem(it.empleado, it.completadas, it.pendientes, it.eficiencia)
                },
                actividadesPorPeriodo = PeriodData(
                    dto.actividadesPorPeriodo.diario,
                    dto.actividadesPorPeriodo.semanal,
                    dto.actividadesPorPeriodo.mensual
                ),
                incidenciasPorCategoria = dto.incidenciasPorCategoria,
                materialesMasUsados = dto.materialesMasUsados.map { MaterialUsage(it.nombre, it.cantidadUtilizada, it.porcentaje) },
                inventarioDisponible = InventorySummary(
                    dto.inventarioDisponible.totalItems,
                    dto.inventarioDisponible.disponibles,
                    dto.inventarioDisponible.stockBajo,
                    dto.inventarioDisponible.agotados
                ),
                kpis = dto.kpis.map {
                    Kpi(it.nombre, it.valor, it.objetivo, try { KpiTrend.valueOf(it.tendencia) } catch (_: Exception) { KpiTrend.ESTABLE })
                }
            )
        } else throw Exception("Error al obtener analytics: ${response.code()}")
    }

    override suspend fun getDailySummary(): Result<DailySummaryData> = runCatching {
        val response = analyticsService.getDailySummary()
        if (response.isSuccessful) {
            val dto = response.body() ?: throw Exception("Respuesta vacía")
            DailySummaryData(
                actividadesCompletadas = dto.actividadesCompletadas,
                actividadesPendientes = dto.actividadesPendientes,
                misionesVencidas = dto.misionesVencidas,
                incidenciasActivas = dto.incidenciasActivas,
                productividad = dto.productividad,
                horasTrabajadas = dto.horasTrabajadas
            )
        } else throw Exception("Error al obtener resumen diario: ${response.code()}")
    }

    override suspend fun getWeeklySummary(): Result<SummaryData> = runCatching {
        val response = analyticsService.getWeeklySummary()
        if (response.isSuccessful) {
            val dto = response.body() ?: throw Exception("Respuesta vacía")
            SummaryData(
                actividadesRealizadas = dto.actividadesRealizadas,
                maquinasCompletadas = dto.maquinasCompletadas,
                materialesUtilizados = dto.materialesUtilizados,
                incidenciasReportadas = dto.incidenciasReportadas,
                misionesCompletadas = dto.misionesCompletadas,
                productividadPromedio = dto.productividadPromedio
            )
        } else throw Exception("Error al obtener resumen semanal: ${response.code()}")
    }

    override suspend fun getMonthlySummary(): Result<SummaryData> = runCatching {
        val response = analyticsService.getMonthlySummary()
        if (response.isSuccessful) {
            val dto = response.body() ?: throw Exception("Respuesta vacía")
            SummaryData(
                actividadesRealizadas = dto.actividadesRealizadas,
                maquinasCompletadas = dto.maquinasCompletadas,
                materialesUtilizados = dto.materialesUtilizados,
                incidenciasReportadas = dto.incidenciasReportadas,
                misionesCompletadas = dto.misionesCompletadas,
                productividadPromedio = dto.productividadPromedio
            )
        } else throw Exception("Error al obtener resumen mensual: ${response.code()}")
    }
}
