package com.biobox.biotech.presentation.incidents

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.biobox.biotech.core.common.UiState
import com.biobox.biotech.domain.model.IncidentSeverity
import com.biobox.biotech.domain.model.IncidentStatus
import com.biobox.biotech.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentListScreen(
    onIncidentClick: (Int) -> Unit,
    onCreateIncident: () -> Unit,
    viewModel: IncidentViewModel = hiltViewModel()
) {
    val state by viewModel.incidents.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Incidencias", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulOscuro, titleContentColor = Blanco),
                actions = {
                    IconButton(onClick = onCreateIncident) {
                        Icon(Icons.Default.Add, contentDescription = "Nueva incidencia", tint = Blanco)
                    }
                }
            )
        }
    ) { padding ->
        when (val s = state) {
            is UiState.Loading -> Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AzulOscuro)
            }
            is UiState.Error -> Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(s.message, color = Rojo)
            }
            is UiState.Success -> {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(s.data) { incident ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onIncidentClick(incident.id) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = GrisCard),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(44.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        when (incident.gravedad) {
                                            IncidentSeverity.CRITICA -> Icons.Default.Error
                                            IncidentSeverity.ALTA -> Icons.Default.Warning
                                            IncidentSeverity.MEDIA -> Icons.Default.Info
                                            IncidentSeverity.BAJA -> Icons.Default.CheckCircleOutline
                                        },
                                        contentDescription = null,
                                        tint = when (incident.gravedad) {
                                            IncidentSeverity.CRITICA -> Rojo
                                            IncidentSeverity.ALTA -> Naranja
                                            IncidentSeverity.MEDIA -> Naranja.copy(alpha = 0.5f)
                                            IncidentSeverity.BAJA -> VerdePrincipal
                                        },
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(incident.titulo, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AzulOscuro)
                                    Text(incident.categoria.name, fontSize = 12.sp, color = Gris600)
                                    incident.maquinaNombre?.let {
                                        Text(it, fontSize = 11.sp, color = Gris500)
                                    }
                                }
                                Text(incident.estado.name, fontSize = 10.sp, color = when (incident.estado) {
                                    IncidentStatus.RESUELTO, IncidentStatus.CERRADO -> VerdePrincipal
                                    IncidentStatus.EN_RESOLUCION -> Naranja
                                    else -> AzulOscuro
                                })
                            }
                        }
                    }
                }
            }
            else -> {}
        }
    }
}
