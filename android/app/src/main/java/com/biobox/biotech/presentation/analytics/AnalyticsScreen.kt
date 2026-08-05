package com.biobox.biotech.presentation.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.biobox.biotech.core.common.UiState
import com.biobox.biotech.domain.model.*
import com.biobox.biotech.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val analyticsState by viewModel.analyticsState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Dashboard Analítico", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = AzulOscuro)
        }

        when (val state = analyticsState) {
            is UiState.Loading -> item {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AzulOscuro)
                }
            }
            is UiState.Error -> item {
                Text(state.message, color = Rojo)
            }
            is UiState.Success -> {
                val data = state.data

                item { KpiSection(data.kpis) }
                item { ProgressChartCard("Avance por Proyecto", data.avancePorProyecto) }
                item { ProgressChartCard("Avance por Máquina", data.avancePorMaquina) }
                item { ProductivityCard(data.productividadPorEmpleado) }
                item { PeriodCard(data.actividadesPorPeriodo) }
                item { MaterialUsageCard(data.materialesMasUsados) }
                item { InventoryCard(data.inventarioDisponible) }
            }
            else -> {}
        }
    }
}

@Composable
fun KpiSection(kpis: List<Kpi>) {
    Column {
        Text("Indicadores Clave", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AzulOscuro)
        Spacer(modifier = Modifier.height(8.dp))
        kpis.chunked(2).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { kpi ->
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = GrisCard)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(kpi.nombre, fontSize = 11.sp, color = Gris600)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(kpi.valor, fontWeight = FontWeight.Black, fontSize = 22.sp, color = AzulOscuro)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    when (kpi.tendencia) {
                                        KpiTrend.ALCISTA -> Icons.Default.TrendingUp
                                        KpiTrend.BAJISTA -> Icons.Default.TrendingDown
                                        KpiTrend.ESTABLE -> Icons.Default.TrendingFlat
                                    },
                                    contentDescription = null,
                                    tint = when (kpi.tendencia) {
                                        KpiTrend.ALCISTA -> VerdePrincipal
                                        KpiTrend.BAJISTA -> Rojo
                                        KpiTrend.ESTABLE -> Naranja
                                    },
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressChartCard(title: String, items: List<ProgressItem>) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = GrisCard)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = AzulOscuro)
            Spacer(modifier = Modifier.height(12.dp))
            items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item.nombre, modifier = Modifier.width(100.dp), fontSize = 12.sp, color = Gris700)
                    LinearProgressIndicator(
                        progress = { item.porcentaje / 100f },
                        modifier = Modifier.weight(1f).height(8.dp),
                        color = if (item.porcentaje >= 100) VerdePrincipal else if (item.porcentaje >= 50) Naranja else Rojo,
                        trackColor = Color.LightGray.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${item.porcentaje}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AzulOscuro)
                }
            }
        }
    }
}

@Composable
fun ProductivityCard(items: List<ProductivityItem>) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = GrisCard)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Productividad por Empleado", fontWeight = FontWeight.Bold, color = AzulOscuro)
            Spacer(modifier = Modifier.height(12.dp))
            items.forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item.empleado, modifier = Modifier.weight(1f), fontSize = 13.sp, color = Gris700)
                    Text("${item.completadas}/${item.completadas + item.pendientes}", fontSize = 12.sp, color = Gris600)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${(item.eficiencia * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AzulOscuro)
                }
            }
        }
    }
}

@Composable
fun PeriodCard(period: PeriodData) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = GrisCard)) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            PeriodStat("Hoy", period.diario, Icons.Default.Today)
            PeriodStat("Semana", period.semanal, Icons.Default.DateRange)
            PeriodStat("Mes", period.mensual, Icons.Default.CalendarMonth)
        }
    }
}

@Composable
fun PeriodStat(label: String, count: Int, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = AzulOscuro, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(count.toString(), fontWeight = FontWeight.Black, fontSize = 20.sp, color = AzulOscuro)
        Text(label, fontSize = 11.sp, color = Gris600)
    }
}

@Composable
fun MaterialUsageCard(items: List<MaterialUsage>) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = GrisCard)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Materiales Más Usados", fontWeight = FontWeight.Bold, color = AzulOscuro)
            Spacer(modifier = Modifier.height(12.dp))
            items.take(5).forEach { item ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item.nombre, modifier = Modifier.weight(1f), fontSize = 13.sp, color = Gris700)
                    Text("${item.cantidadUtilizada}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AzulOscuro)
                }
            }
        }
    }
}

@Composable
fun InventoryCard(inventory: InventorySummary) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = GrisCard)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Inventario Disponible", fontWeight = FontWeight.Bold, color = AzulOscuro)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                InventoryStat("Total", inventory.totalItems, AzulOscuro)
                InventoryStat("Disponible", inventory.disponibles, VerdePrincipal)
                InventoryStat("Stock Bajo", inventory.stockBajo, Naranja)
                InventoryStat("Agotado", inventory.agotados, Rojo)
            }
        }
    }
}

@Composable
fun InventoryStat(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count.toString(), fontWeight = FontWeight.Black, fontSize = 22.sp, color = color)
        Text(label, fontSize = 10.sp, color = Gris600)
    }
}
