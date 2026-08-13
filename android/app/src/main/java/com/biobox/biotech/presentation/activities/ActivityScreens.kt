@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.biobox.biotech.presentation.activities

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.biobox.biotech.BuildConfig
import com.biobox.biotech.core.common.UiState
import com.biobox.biotech.domain.model.Activity
import com.biobox.biotech.domain.model.ActivityStatus
import com.biobox.biotech.presentation.components.navigation.BioTechTopBar
import com.biobox.biotech.presentation.theme.DarkBackground

@Composable
fun ActivityDetailScreen(id: Int, onBack: () -> Unit, viewModel: ActivityViewModel = hiltViewModel()) {
    val state by viewModel.currentActivity.collectAsState()
    var evidenceToDelete by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(id) { viewModel.loadActivity(id) }
    LaunchedEffect(Unit) {
        viewModel.operationEvents.collect { msg -> snackbarHostState.showSnackbar(msg) }
    }
    Scaffold(
        topBar = { BioTechTopBar("DETALLE DE ACTIVIDAD") },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when (val current = state) {
                is UiState.Success -> {
                    val activity = current.data
                    Text(activity.titulo, style = MaterialTheme.typography.headlineSmall)
                    Text(activity.descripcion.orEmpty())
                    Text("Responsable: ${activity.responsable}")
                    Text("Tiempo: ${activity.tiempoEmpleado} minutos")
                    Text("Estado: ${activity.estado.name}")
                    ActivityEvidenceGallery(activity, onDelete = { evidenceToDelete = it })
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
    evidenceToDelete?.let { url ->
        AlertDialog(
            onDismissRequest = { evidenceToDelete = null },
            title = { Text("Eliminar foto") },
            text = { Text("¿Deseas eliminar esta evidencia de la actividad?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteActivityEvidence(id, url)
                    evidenceToDelete = null
                }) { Text("Eliminar") }
            },
            dismissButton = { TextButton(onClick = { evidenceToDelete = null }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun ActivityEvidenceGallery(activity: Activity, onDelete: (String) -> Unit) {
    if (activity.evidencias.isEmpty()) {
        Text("Sin fotos adjuntas.")
        return
    }
    Text("Fotos", style = MaterialTheme.typography.titleMedium)
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        activity.evidencias.forEach { url ->
            Card(modifier = Modifier.width(120.dp)) {
                Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    AsyncImage(
                        model = absoluteEvidenceUrl(url),
                        contentDescription = "Evidencia",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(84.dp)
                    )
                    OutlinedButton(onClick = { onDelete(url) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Eliminar")
                    }
                }
            }
        }
    }
}

private fun absoluteEvidenceUrl(url: String): String {
    if (url.startsWith("http://") || url.startsWith("https://")) return url
    return BuildConfig.API_BASE_URL.trimEnd('/') + "/" + url.trimStart('/')
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
