package com.biobox.biotech.presentation.missions

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
import com.biobox.biotech.domain.model.MissionPriority
import com.biobox.biotech.domain.model.MissionStatus
import com.biobox.biotech.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionListScreen(
    onMissionClick: (Int) -> Unit,
    onCreateMission: () -> Unit,
    onCompletedMissions: () -> Unit,
    viewModel: MissionViewModel = hiltViewModel()
) {
    val state by viewModel.missions.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Misiones", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulOscuro, titleContentColor = Blanco),
                actions = {
                    IconButton(onClick = onCompletedMissions) {
                        Icon(Icons.Default.History, contentDescription = "Misiones cumplidas", tint = Blanco)
                    }
                    IconButton(onClick = onCreateMission) {
                        Icon(Icons.Default.Add, contentDescription = "Nueva misión", tint = Blanco)
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
                    items(s.data) { mission ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onMissionClick(mission.id) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = GrisCard),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Circle,
                                        contentDescription = null,
                                        tint = when (mission.prioridad) {
                                            MissionPriority.CRITICA -> Rojo
                                            MissionPriority.ALTA -> Naranja
                                            MissionPriority.MEDIA -> Naranja.copy(alpha = 0.5f)
                                            MissionPriority.BAJA -> VerdePrincipal
                                        },
                                        modifier = Modifier.size(8.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(mission.titulo, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AzulOscuro)
                                    Text(mission.asignadoA, fontSize = 12.sp, color = Gris600)
                                    Text(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(mission.fechaLimite)), fontSize = 11.sp, color = Gris500)
                                }
                                Text(mission.estado.name, fontSize = 10.sp, color = when (mission.estado) {
                                    MissionStatus.COMPLETADA, MissionStatus.APROBADA -> VerdePrincipal
                                    MissionStatus.VENCIDA -> Rojo
                                    MissionStatus.EN_CURSO -> Naranja
                                    MissionStatus.CANCELADA -> Gris600
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
