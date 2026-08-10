@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.biobox.biotech.presentation.goals

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.biobox.biotech.core.common.UiState
import com.biobox.biotech.domain.model.Goal
import com.biobox.biotech.domain.model.GoalStatus
import com.biobox.biotech.presentation.components.navigation.BioTechTopBar
import com.biobox.biotech.presentation.theme.DarkBackground

@Composable
fun GoalDetailScreen(id: Int, onBack: () -> Unit, viewModel: GoalViewModel = hiltViewModel()) {
    val state by viewModel.currentGoal.collectAsState(); LaunchedEffect(id) { viewModel.loadGoal(id) }
    Scaffold(topBar = { BioTechTopBar("DETALLE DE META") }, containerColor = DarkBackground) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when (val current = state) {
                is UiState.Success -> { Text(current.data.titulo, style = MaterialTheme.typography.headlineSmall); Text(current.data.descripcion.orEmpty()); Text("Proyecto: ${current.data.proyecto.orEmpty()}"); Text("Avance: ${current.data.porcentajeAvance}%"); Text("Estado: ${current.data.estado.name}"); OutlinedButton(onClick = { viewModel.deleteGoal(id); onBack() }) { Text("Eliminar") } }
                is UiState.Error -> Text(current.message, color = MaterialTheme.colorScheme.error)
                else -> CircularProgressIndicator()
            }
            OutlinedButton(onClick = onBack) { Text("Regresar") }
        }
    }
}

@Composable
fun CreateGoalScreen(onCreated: () -> Unit, onBack: () -> Unit, viewModel: GoalViewModel = hiltViewModel()) {
    var title by remember { mutableStateOf("") }; var description by remember { mutableStateOf("") }; var project by remember { mutableStateOf("") }
    Scaffold(topBar = { BioTechTopBar("NUEVA META") }, containerColor = DarkBackground) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(title, { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(description, { description = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(project, { project = it }, label = { Text("Proyecto") }, modifier = Modifier.fillMaxWidth())
            Button(enabled = title.isNotBlank(), onClick = { val now = System.currentTimeMillis(); viewModel.createGoal(Goal(-(now % Int.MAX_VALUE).toInt(), title.trim(), description.trim(), project.trim(), fechaInicio = now, fechaFin = now + 30L * 86400000, estado = GoalStatus.NO_INICIADA), onCreated) }, modifier = Modifier.fillMaxWidth()) { Text("Guardar meta") }
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
        }
    }
}
