@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.biobox.biotech.presentation.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.biobox.biotech.presentation.theme.*

@Composable
fun NotificationsScreen(viewModel: AlertViewModel = hiltViewModel()) {
    val alerts by viewModel.alerts.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text("Alertas de incidencias", fontWeight = FontWeight.Bold) }) }) { padding ->
        if (alerts.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.NotificationsNone, contentDescription = null, modifier = Modifier.size(64.dp), tint = Gris300)
                    Text("No hay alertas pendientes", color = Gris600)
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(alerts, key = { it.id }) { alert ->
                    Card(colors = CardDefaults.cardColors(containerColor = Error.copy(alpha = 0.12f))) {
                        Column(Modifier.fillMaxWidth().padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Error)
                                Spacer(Modifier.width(8.dp)); Text("${alert.gravedad} · ${alert.codigoMaquina}", fontWeight = FontWeight.Bold, color = Error)
                            }
                            Text(alert.mensaje, modifier = Modifier.padding(vertical = 8.dp))
                            Button(onClick = { viewModel.acknowledge(alert.id) }) { Text("Marcar atendida") }
                        }
                    }
                }
            }
        }
    }
}
