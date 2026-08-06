package com.biobox.biotech.presentation.inspections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biobox.biotech.core.common.UiState
import com.biobox.biotech.domain.model.InspectionSummary
import com.biobox.biotech.presentation.components.loading.LoadingView
import com.biobox.biotech.presentation.components.states.EmptyState
import com.biobox.biotech.presentation.components.states.ErrorState
import com.biobox.biotech.presentation.theme.AzulOscuro
import com.biobox.biotech.presentation.theme.Blanco
import com.biobox.biotech.presentation.theme.DarkBackground
import com.biobox.biotech.presentation.theme.GrisCard
import com.biobox.biotech.presentation.theme.PrimaryGreen
import com.biobox.biotech.presentation.theme.Rojo
import com.biobox.biotech.presentation.theme.TextSecondaryDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectionListScreen(
    onCreateInspection: () -> Unit,
    viewModel: InspectionListViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var statusFilter by remember { mutableStateOf<String?>(null) }
    val filters = listOf("Pendiente", "En Proceso", "Completada", "Completa", "Incompleta")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Revisiones", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulOscuro, titleContentColor = Blanco)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateInspection, containerColor = PrimaryGreen) {
                Icon(Icons.Default.Add, contentDescription = "Nueva revision")
            }
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = statusFilter == null, onClick = { statusFilter = null }, label = { Text("Todas") })
                filters.take(3).forEach { status ->
                    FilterChip(selected = statusFilter == status, onClick = { statusFilter = status }, label = { Text(status) })
                }
            }
            when (val current = state) {
                is UiState.Loading -> LoadingView()
                is UiState.Error -> ErrorState(message = current.message, onRetry = viewModel::loadInspections)
                is UiState.Success -> {
                    val rows = current.data.filter { statusFilter == null || normalizeStatus(it.status) == statusFilter }
                    if (rows.isEmpty()) {
                        EmptyState(
                            title = "Sin revisiones",
                            description = "Crea una revisión desde una máquina o usa el botón de agregar.",
                            icon = Icons.Default.FactCheck
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            items(rows, key = { it.id }) { InspectionCard(it) }
                        }
                    }
                }
                else -> Unit
            }
        }
    }
}

@Composable
private fun InspectionCard(item: InspectionSummary) {
    val progress = item.progress.coerceIn(0.0, 100.0).toFloat()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = GrisCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FactCheck, contentDescription = null, tint = if (progress >= 100f) PrimaryGreen else Rojo)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.machineCode, style = MaterialTheme.typography.labelLarge, color = PrimaryGreen, fontWeight = FontWeight.Bold)
                    Text(item.machineName, style = MaterialTheme.typography.titleMedium, color = Blanco, fontWeight = FontWeight.Bold)
                    Text("Auditor: ${item.auditor.ifBlank { "Sin asignar" }}", color = TextSecondaryDark, style = MaterialTheme.typography.bodySmall)
                }
                Text(normalizeStatus(item.status), color = TextSecondaryDark, style = MaterialTheme.typography.labelMedium)
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(progress = { progress / 100f }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(4.dp))
            Text("${progress.toInt()}% completado", color = TextSecondaryDark, style = MaterialTheme.typography.labelSmall)
        }
    }
}

private fun normalizeStatus(value: String): String = when (value.uppercase()) {
    "COMPLETO", "COMPLETA" -> "Completada"
    "INCOMPLETO", "INCOMPLETA" -> "En Proceso"
    else -> value.ifBlank { "Pendiente" }
}
