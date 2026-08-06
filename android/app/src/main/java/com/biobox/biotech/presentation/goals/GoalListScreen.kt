package com.biobox.biotech.presentation.goals

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.biobox.biotech.core.common.UiState
import com.biobox.biotech.domain.model.GoalStatus
import com.biobox.biotech.presentation.components.states.EmptyState
import com.biobox.biotech.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalListScreen(
    onGoalClick: (Int) -> Unit,
    onCreateGoal: () -> Unit,
    viewModel: GoalViewModel = hiltViewModel()
) {
    val state by viewModel.goals.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Metas", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulOscuro, titleContentColor = Blanco),
                actions = {
                    IconButton(onClick = onCreateGoal) {
                        Icon(Icons.Default.Add, contentDescription = "Nueva meta", tint = Blanco)
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
                            title = "Sin metas registradas",
                            description = "Agrega una meta para dar seguimiento al avance operativo.",
                            icon = Icons.Default.Flag,
                            action = {
                                Button(onClick = onCreateGoal, colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("AGREGAR META")
                                }
                            }
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(s.data) { goal ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { onGoalClick(goal.id) },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = GrisCard),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(goal.titulo, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Blanco, modifier = Modifier.weight(1f))
                                        Text(goal.estado.name, fontSize = 11.sp, color = when (goal.estado) {
                                            GoalStatus.COMPLETADA -> VerdePrincipal
                                            GoalStatus.EN_PROGRESO -> Naranja
                                            GoalStatus.CANCELADA -> Rojo
                                            GoalStatus.NO_INICIADA -> TextSecondaryDark
                                        })
                                    }
                                    goal.proyecto?.let {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(it, fontSize = 12.sp, color = TextSecondaryDark)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = { goal.porcentajeAvance / 100f },
                                        modifier = Modifier.fillMaxWidth().height(8.dp),
                                        color = if (goal.porcentajeAvance >= 100) VerdePrincipal else if (goal.porcentajeAvance >= 50) Naranja else Rojo,
                                        trackColor = Color.LightGray.copy(alpha = 0.3f)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${goal.porcentajeAvance}% completado", fontSize = 11.sp, color = TextSecondaryDark)
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
