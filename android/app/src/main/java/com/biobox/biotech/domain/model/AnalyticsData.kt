package com.biobox.biotech.domain.model

data class AnalyticsData(
    val avancePorProyecto: List<ProgressItem> = emptyList(),
    val avancePorMaquina: List<ProgressItem> = emptyList(),
    val productividadPorEmpleado: List<ProductivityItem> = emptyList(),
    val actividadesPorPeriodo: PeriodData = PeriodData(),
    val incidenciasPorCategoria: Map<String, Int> = emptyMap(),
    val materialesMasUsados: List<MaterialUsage> = emptyList(),
    val inventarioDisponible: InventorySummary = InventorySummary(),
    val kpis: List<Kpi> = emptyList()
)

data class ProgressItem(
    val nombre: String,
    val porcentaje: Int,
    val meta: Int = 100
)

data class ProductivityItem(
    val empleado: String,
    val completadas: Int,
    val pendientes: Int,
    val eficiencia: Float = 0f
)

data class PeriodData(
    val diario: Int = 0,
    val semanal: Int = 0,
    val mensual: Int = 0
)

data class MaterialUsage(
    val nombre: String,
    val cantidadUtilizada: Int,
    val porcentaje: Float = 0f
)

data class InventorySummary(
    val totalItems: Int = 0,
    val disponibles: Int = 0,
    val stockBajo: Int = 0,
    val agotados: Int = 0
)

data class Kpi(
    val nombre: String,
    val valor: String,
    val objetivo: String? = null,
    val tendencia: KpiTrend = KpiTrend.ESTABLE
)

enum class KpiTrend {
    ALCISTA, BAJISTA, ESTABLE
}
