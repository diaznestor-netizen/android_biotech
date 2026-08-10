@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.biobox.biotech.presentation.activities

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.biobox.biotech.core.common.UiState
import com.biobox.biotech.domain.model.Activity
import com.biobox.biotech.domain.model.ActivityStatus
import com.biobox.biotech.presentation.components.navigation.BioTechTopBar
import com.biobox.biotech.presentation.theme.DarkBackground

@Composable
fun ActivityDetailScreen(id: Int, onBack: () -> Unit, viewModel: ActivityViewModel = hiltViewModel()) {
    val state by viewModel.currentActivity.collectAsState()
    LaunchedEffect(id) { viewModel.loadActivity(id) }
    Scaffold(topBar = { BioTechTopBar("DETALLE DE ACTIVIDAD") }, containerColor = DarkBackground) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when (val current = state) {
                is UiState.Success -> {
                    Text(current.data.titulo, style = MaterialTheme.typography.headlineSmall)
                    Text(current.data.descripcion.orEmpty())
                    Text("Responsable: ${current.data.responsable}")
                    Text("Tiempo: ${current.data.tiempoEmpleado} minutos")
                    Text("Estado: ${current.data.estado.name}")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { viewModel.approveActivity(id) }) { Text("Aprobar") }
                        OutlinedButton(onClick = { viewModel.rejectActivity(id, "Requiere corrección") }) { Text("Rechazar") }
                    }
                }
                is UiState.Error -> Text(current.message, color = MaterialTheme.colorScheme.error)
                else -> CircularProgressIndicator()
            }
            OutlinedButton(onClick = onBack) { Text("Regresar") }
        }
    }
}

@Composable
fun CreateActivityScreen(onCreated: () -> Unit, onBack: () -> Unit, viewModel: ActivityViewModel = hiltViewModel()) {
    var title by remember { mutableStateOf("") }; var description by remember { mutableStateOf("") }
    var responsible by remember { mutableStateOf("") }; var minutes by remember { mutableStateOf("0") }
    Scaffold(topBar = { BioTechTopBar("NUEVA ACTIVIDAD") }, containerColor = DarkBackground) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(title, { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(description, { description = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(responsible, { responsible = it }, label = { Text("Responsable") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(minutes, { minutes = it.filter(Char::isDigit) }, label = { Text("Minutos") }, modifier = Modifier.fillMaxWidth())
            Button(
                enabled = title.isNotBlank() && responsible.isNotBlank(),
                onClick = {
                    viewModel.createActivity(Activity(tempId(), title.trim(), description.trim(), responsible.trim(), tiempoEmpleado = minutes.toIntOrNull() ?: 0, fecha = System.currentTimeMillis(), estado = ActivityStatus.PENDIENTE), onCreated)
                }, modifier = Modifier.fillMaxWidth()
            ) { Text("Guardar actividad") }
            OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Cancelar") }
        }
    }
}

private fun tempId() = -(System.currentTimeMillis() % Int.MAX_VALUE).toInt()
