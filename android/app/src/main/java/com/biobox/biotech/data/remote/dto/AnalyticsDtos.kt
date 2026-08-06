package com.biobox.biotech.data.remote.dto

data class AnalyticsDto(
    val avancePorProyecto: List<ProgressItemDto> = emptyList(),
    val avancePorMaquina: List<ProgressItemDto> = emptyList(),
    val productividadPorEmpleado: List<ProductivityItemDto> = emptyList(),
    val actividadesPorPeriodo: PeriodDataDto = PeriodDataDto(),
    val incidenciasPorCategoria: Map<String, Int> = emptyMap(),
    val materialesMasUsados: List<MaterialUsageDto> = emptyList(),
    val inventarioDisponible: InventorySummaryDto = InventorySummaryDto(),
    val kpis: List<KpiDto> = emptyList()
)

data class ProgressItemDto(
    val nombre: String,
    val porcentaje: Int,
    val meta: Int = 100
)

data class ProductivityItemDto(
    val empleado: String,
    val completadas: Int,
    val pendientes: Int,
    val eficiencia: Float = 0f
)

data class PeriodDataDto(
    val diario: Int = 0,
    val semanal: Int = 0,
    val mensual: Int = 0
)

data class MaterialUsageDto(
    val nombre: String,
    val cantidadUtilizada: Int,
    val porcentaje: Float = 0f
)

data class InventorySummaryDto(
    val totalItems: Int = 0,
    val disponibles: Int = 0,
    val stockBajo: Int = 0,
    val agotados: Int = 0
)

data class KpiDto(
    val nombre: String,
    val valor: String,
    val objetivo: String? = null,
    val tendencia: String = "ESTABLE"
)

data class DailySummaryDto(
    val actividadesCompletadas: Int = 0,
    val actividadesPendientes: Int = 0,
    val misionesVencidas: Int = 0,
    val incidenciasActivas: Int = 0,
    val productividad: Float = 0f,
    val horasTrabajadas: Float = 0f
)

data class SummaryDataDto(
    val actividadesRealizadas: Int = 0,
    val maquinasCompletadas: Int = 0,
    val materialesUtilizados: Int = 0,
    val incidenciasReportadas: Int = 0,
    val misionesCompletadas: Int = 0,
    val productividadPromedio: Float = 0f
)
