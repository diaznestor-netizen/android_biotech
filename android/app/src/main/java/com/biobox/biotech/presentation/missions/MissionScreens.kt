@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.biobox.biotech.presentation.missions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun MissionDetailScreen(id: Int, onBack: () -> Unit, viewModel: MissionViewModel = hiltViewModel()) {
    val state by viewModel.currentMission.collectAsState(); LaunchedEffect(id) { viewModel.loadMission(id) }
    Scaffold(topBar = { BioTechTopBar("DETALLE DE MISIÓN") }, containerColor = DarkBackground) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when (val current = state) {
                is UiState.Success -> { Text(current.data.titulo, style = MaterialTheme.typography.headlineSmall); Text(current.data.descripcion.orEmpty()); Text("Responsable: ${current.data.asignadoA}"); Text("Prioridad: ${current.data.prioridad.name}"); Text("Estado: ${current.data.estado.name}"); Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Button(onClick = { viewModel.completeMission(id, "Completada desde Android") }) { Text("Completar") }; OutlinedButton(onClick = { viewModel.approveMission(id) }) { Text("Aprobar") } } }
                is UiState.Error -> Text(current.message, color = MaterialTheme.colorScheme.error)
                else -> CircularProgressIndicator()
            }
            OutlinedButton(onClick = onBack) { Text("Regresar") }
        }
    }
}

@Composable
fun CreateMissionScreen(onCreated: () -> Unit, onBack: () -> Unit, viewModel: MissionViewModel = hiltViewModel()) {
    var title by remember { mutableStateOf("") }; var description by remember { mutableStateOf("") }; var assignedTo by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(MissionPriority.MEDIA) }
    Scaffold(topBar = { BioTechTopBar("NUEVA MISIÓN") }, containerColor = DarkBackground) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(title, { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(description, { description = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(assignedTo, { assignedTo = it }, label = { Text("Responsable") }, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { MissionPriority.entries.forEach { FilterChip(priority == it, { priority = it }, { Text(it.name) }) } }
            Button(enabled = title.isNotBlank() && assignedTo.isNotBlank(), onClick = { val now = System.currentTimeMillis(); viewModel.createMission(Mission(-(now % Int.MAX_VALUE).toInt(), title.trim(), description.trim(), assignedTo.trim(), fechaLimite = now + 7L * 86400000, prioridad = priority, estado = MissionStatus.PENDIENTE), onCreated) }, modifier = Modifier.fillMaxWidth()) { Text("Guardar misión") }
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
        }
    }
}

@Composable
fun CompletedMissionsScreen(onBack: () -> Unit, viewModel: MissionViewModel = hiltViewModel()) {
    val state by viewModel.completedMissions.collectAsState(); LaunchedEffect(Unit) { viewModel.loadCompletedMissions() }
    Scaffold(topBar = { BioTechTopBar("MISIONES CUMPLIDAS") }, containerColor = DarkBackground) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            when (val current = state) {
                is UiState.Success -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) { items(current.data) { Card { Column(Modifier.fillMaxWidth().padding(14.dp)) { Text(it.titulo, style = MaterialTheme.typography.titleMedium); Text("${it.asignadoA} · ${it.estado.name}") } } } }
                is UiState.Error -> Text(current.message, color = MaterialTheme.colorScheme.error)
                else -> CircularProgressIndicator()
            }
            OutlinedButton(onClick = onBack) { Text("Regresar") }
        }
    }
}
