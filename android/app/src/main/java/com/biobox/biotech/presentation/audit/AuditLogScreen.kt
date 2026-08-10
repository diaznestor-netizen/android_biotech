@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.biobox.biotech.presentation.audit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.biobox.biotech.core.common.UiState
import com.biobox.biotech.data.remote.api.HistoryEntryDto
import com.biobox.biotech.presentation.components.navigation.BioTechTopBar
import com.biobox.biotech.presentation.components.textfields.BioTechSearchBar
import com.biobox.biotech.presentation.theme.*

@Composable
fun AuditLogScreen(viewModel: AuditViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    var query by remember { mutableStateOf("") }
    Scaffold(topBar = { BioTechTopBar("HISTORIAL GLOBAL") }, containerColor = DarkBackground) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Box(Modifier.padding(16.dp)) {
                BioTechSearchBar(query, { query = it }, "Filtrar por usuario, origen o acción...")
            }
            when (val current = state) {
                UiState.Loading -> LinearProgressIndicator(Modifier.fillMaxWidth())
                is UiState.Error -> Column(Modifier.padding(16.dp)) {
                    Text(current.message, color = Error)
                    TextButton(onClick = viewModel::load) { Text("Reintentar") }
                }
                is UiState.Success -> {
                    val rows = current.data.filter {
                        query.isBlank() || listOf(it.usuario, it.origen, it.accion, it.detalle, it.entidad)
                            .any { value -> value.contains(query, ignoreCase = true) }
                    }
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(rows, key = { "${it.fecha}-${it.origen}-${it.registro}-${it.accion}" }) { HistoryCard(it) }
                    }
                }
                else -> Unit
            }
        }
    }
}

@Composable
private fun HistoryCard(item: HistoryEntryDto) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(item.origen, color = PrimaryCyan, fontWeight = FontWeight.Bold)
                Text(item.fecha.take(16).replace('T', ' '), color = TextSecondaryDark, style = MaterialTheme.typography.labelSmall)
            }
            Text(item.accion, fontWeight = FontWeight.Bold)
            Text(item.detalle.ifBlank { "${item.entidad} #${item.registro}" })
            Text(item.usuario.ifBlank { "Sistema" }, color = TextSecondaryDark, style = MaterialTheme.typography.labelSmall)
        }
    }
}
