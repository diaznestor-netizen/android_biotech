package com.biobox.biotech.presentation.activities

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
import com.biobox.biotech.domain.model.ActivityStatus
import com.biobox.biotech.presentation.components.states.EmptyState
import com.biobox.biotech.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityListScreen(
    onActivityClick: (Int) -> Unit,
    onCreateActivity: () -> Unit,
    viewModel: ActivityViewModel = hiltViewModel()
) {
    val state by viewModel.activities.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Actividades", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulOscuro, titleContentColor = Blanco),
                actions = {
                    IconButton(onClick = onCreateActivity) {
                        Icon(Icons.Default.Add, contentDescription = "Nueva actividad", tint = Blanco)
                    }
                }
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        when (val s = state) {
            is UiState.Loading -> Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
            is UiState.Error -> Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(s.message, color = Rojo)
            }
            is UiState.Success -> {
                if (s.data.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                        EmptyState(
                            title = "Sin actividades programadas",
                            description = "Crea una actividad para registrar avances y responsables.",
                            icon = Icons.Default.Assignment,
                            action = {
                                Button(onClick = onCreateActivity, colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("AGREGAR ACTIVIDAD")
                                }
                            }
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(s.data) { activity ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { onActivityClick(activity.id) },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = GrisCard),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when (activity.estado) {
                                            ActivityStatus.APROBADA -> Icons.Default.CheckCircle
                                            ActivityStatus.RECHAZADA -> Icons.Default.Cancel
                                            ActivityStatus.COMPLETADA -> Icons.Default.TaskAlt
                                            ActivityStatus.EN_CURSO -> Icons.Default.PlayCircle
                                            ActivityStatus.PENDIENTE -> Icons.Default.Schedule
                                            else -> Icons.Default.Assignment
                                        },
                                        contentDescription = null,
                                        tint = when (activity.estado) {
                                            ActivityStatus.APROBADA -> VerdePrincipal
                                            ActivityStatus.RECHAZADA -> Rojo
                                            ActivityStatus.COMPLETADA -> VerdePrincipal
                                            ActivityStatus.EN_CURSO -> Naranja
                                            else -> TextoSecundarioOscuro
                                        },
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(activity.titulo, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Blanco)
                                        Text(activity.responsable, fontSize = 12.sp, color = TextSecondaryDark)
                                        Text(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(activity.fecha)), fontSize = 11.sp, color = TextSecondaryDark)
                                    }
                                    Text("${activity.tiempoEmpleado}min", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                                }
                            }
                        }
                    }
                }
            }
            else -> {}
        }
    }
}
