@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.biobox.biotech.presentation.incidents

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.biobox.biotech.core.common.UiState
import com.biobox.biotech.domain.model.*
import com.biobox.biotech.presentation.components.navigation.BioTechTopBar
import com.biobox.biotech.presentation.theme.DarkBackground

@Composable
fun IncidentDetailScreen(id: Int, onBack: () -> Unit, viewModel: IncidentViewModel = hiltViewModel()) {
    val state by viewModel.currentIncident.collectAsState(); var resolution by remember { mutableStateOf("") }; LaunchedEffect(id) { viewModel.loadIncident(id) }
    Scaffold(topBar = { BioTechTopBar("DETALLE DE INCIDENCIA") }, containerColor = DarkBackground) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when (val current = state) {
                is UiState.Success -> { Text(current.data.titulo, style = MaterialTheme.typography.headlineSmall); Text(current.data.descripcion); Text("${current.data.categoria.name} · ${current.data.gravedad.name}"); Text("Estado: ${current.data.estado.name}"); OutlinedTextField(resolution, { resolution = it }, label = { Text("Resolución") }, modifier = Modifier.fillMaxWidth()); Button(enabled = resolution.isNotBlank(), onClick = { viewModel.resolveIncident(id, resolution.trim()) }) { Text("Marcar resuelta") } }
                is UiState.Error -> Text(current.message, color = MaterialTheme.colorScheme.error)
                else -> CircularProgressIndicator()
            }
            OutlinedButton(onClick = onBack) { Text("Regresar") }
        }
    }
}

@Composable
fun CreateIncidentScreen(onCreated: () -> Unit, onBack: () -> Unit, viewModel: IncidentViewModel = hiltViewModel()) {
    var title by remember { mutableStateOf("") }; var description by remember { mutableStateOf("") }; var category by remember { mutableStateOf(IncidentCategory.OTRO) }; var severity by remember { mutableStateOf(IncidentSeverity.MEDIA) }
    Scaffold(topBar = { BioTechTopBar("NUEVA INCIDENCIA") }, containerColor = DarkBackground) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(title, { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(description, { description = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            Text("Categoría"); Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf(IncidentCategory.MECANICA, IncidentCategory.ELECTRICA, IncidentCategory.MATERIAL, IncidentCategory.OTRO).forEach { FilterChip(category == it, { category = it }, { Text(it.name) }) } }
            Text("Gravedad"); Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { IncidentSeverity.entries.forEach { FilterChip(severity == it, { severity = it }, { Text(it.name) }) } }
            Button(enabled = title.isNotBlank() && description.isNotBlank(), onClick = { val now = System.currentTimeMillis(); viewModel.createIncident(Incident(-(now % Int.MAX_VALUE).toInt(), title.trim(), description.trim(), category, severity, reportadoPor = "Usuario actual", estado = IncidentStatus.REPORTADO), onCreated) }, modifier = Modifier.fillMaxWidth()) { Text("Reportar incidencia") }
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
        }
    }
}
