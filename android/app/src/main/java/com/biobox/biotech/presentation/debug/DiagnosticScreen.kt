package com.biobox.biotech.presentation.debug

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biobox.biotech.presentation.common.SyncStatusViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticScreen(
    syncViewModel: SyncStatusViewModel = hiltViewModel()
) {
    val syncState by syncViewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Diagnóstico") },
                actions = {
                    IconButton(onClick = { syncViewModel.triggerSync() }) {
                        Icon(Icons.Default.Sync, contentDescription = "Forzar Sync")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                DiagnosticCard(
                    title = "Estado de Sincronización",
                    metrics = listOf(
                        "Pendientes" to syncState.pendingOperationsCount.toString(),
                        "Errores" to syncState.errorCount.toString(),
                        "Conflictos" to syncState.conflictCount.toString(),
                        "Sincronizando" to if (syncState.isSyncing) "SÍ" else "NO",
                        "Última vez" to syncState.lastSyncTime
                    )
                )
            }

            item {
                DiagnosticCard(
                    title = "Conectividad",
                    metrics = listOf(
                        "Servidor Online" to if (syncState.isServerConnected) "SÍ" else "NO",
                        "Modo Offline" to if (syncState.isOnline) "NO" else "SÍ"
                    )
                )
            }

            item {
                Button(
                    onClick = { throw RuntimeException("Test Crash BioTech v1.1") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.BugReport, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("FORZAR CRASH (TEST)")
                }
            }
        }
    }
}

@Composable
private fun DiagnosticCard(title: String, metrics: List<Pair<String, String>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            metrics.forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = label, style = MaterialTheme.typography.bodyMedium)
                    Text(text = value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp)
            }
        }
    }
}
