package com.biobox.biotech.presentation.summaries

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biobox.biotech.core.common.UiState
import com.biobox.biotech.presentation.analytics.AnalyticsViewModel
import com.biobox.biotech.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    viewModel: AnalyticsViewModel
) {
    val dailyState by viewModel.dailySummary.collectAsState()
    val weeklyState by viewModel.weeklySummary.collectAsState()
    val monthlyState by viewModel.monthlySummary.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.loadDailySummary()
        viewModel.loadWeeklySummary()
        viewModel.loadMonthlySummary()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resúmenes", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulOscuro, titleContentColor = Blanco)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = AzulOscuro,
                contentColor = Blanco
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Diario") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Semanal") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Mensual") })
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (selectedTab) {
                    0 -> when (val s = dailyState) {
                        is UiState.Loading -> item { Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AzulOscuro) } }
                        is UiState.Error -> item { Text(s.message, color = Rojo) }
                        is UiState.Success -> {
                            item { SummaryCard("Actividades completadas", "${s.data.actividadesCompletadas}") }
                            item { SummaryCard("Actividades pendientes", "${s.data.actividadesPendientes}") }
                            item { SummaryCard("Misiones vencidas", "${s.data.misionesVencidas}") }
                            item { SummaryCard("Incidencias activas", "${s.data.incidenciasActivas}") }
                            item { SummaryCard("Productividad", "${(s.data.productividad * 100).toInt()}%") }
                            item { SummaryCard("Horas trabajadas", "${s.data.horasTrabajadas}h") }
                        }
                        else -> {}
                    }
                    1 -> when (val s = weeklyState) {
                        is UiState.Loading -> item { Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AzulOscuro) } }
                        is UiState.Error -> item { Text(s.message, color = Rojo) }
                        is UiState.Success -> {
                            item { SummaryCard("Actividades realizadas", "${s.data.actividadesRealizadas}") }
                            item { SummaryCard("Máquinas completadas", "${s.data.maquinasCompletadas}") }
                            item { SummaryCard("Materiales utilizados", "${s.data.materialesUtilizados}") }
                            item { SummaryCard("Incidencias reportadas", "${s.data.incidenciasReportadas}") }
                            item { SummaryCard("Misiones completadas", "${s.data.misionesCompletadas}") }
                            item { SummaryCard("Productividad promedio", "${(s.data.productividadPromedio * 100).toInt()}%") }
                        }
                        else -> {}
                    }
                    2 -> when (val s = monthlyState) {
                        is UiState.Loading -> item { Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AzulOscuro) } }
                        is UiState.Error -> item { Text(s.message, color = Rojo) }
                        is UiState.Success -> {
                            item { SummaryCard("Actividades realizadas", "${s.data.actividadesRealizadas}") }
                            item { SummaryCard("Máquinas completadas", "${s.data.maquinasCompletadas}") }
                            item { SummaryCard("Materiales utilizados", "${s.data.materialesUtilizados}") }
                            item { SummaryCard("Incidencias reportadas", "${s.data.incidenciasReportadas}") }
                            item { SummaryCard("Misiones completadas", "${s.data.misionesCompletadas}") }
                            item { SummaryCard("Productividad promedio", "${(s.data.productividadPromedio * 100).toInt()}%") }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GrisCard),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 14.sp, color = Gris700)
            Text(value, fontWeight = FontWeight.Black, fontSize = 22.sp, color = AzulOscuro)
        }
    }
}
