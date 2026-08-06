package com.biobox.biotech.presentation.documents

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
import com.biobox.biotech.domain.model.DocumentType
import com.biobox.biotech.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentListScreen(
    onDocumentClick: (String) -> Unit,
    viewModel: DocumentViewModel = hiltViewModel()
) {
    val state by viewModel.documents.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Documentación", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulOscuro, titleContentColor = Blanco)
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
                if (s.data.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(64.dp), tint = Gris300)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No hay documentos", color = Gris600)
                        }
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(s.data) { doc ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { onDocumentClick(doc.archivoUrl) },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = GrisCard),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        when (doc.tipo) {
                                            DocumentType.MANUAL -> Icons.Default.MenuBook
                                            DocumentType.CERTIFICADO -> Icons.Default.Verified
                                            DocumentType.REPORTE_TECNICO -> Icons.Default.Assessment
                                            DocumentType.PLANO -> Icons.Default.Draw
                                            else -> Icons.Default.Description
                                        },
                                        contentDescription = null,
                                        tint = AzulOscuro,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(doc.titulo, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AzulOscuro)
                                        Text(doc.tipo.name, fontSize = 12.sp, color = Gris600)
                                        Text(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(doc.fechaSubida)), fontSize = 11.sp, color = Gris500)
                                    }
                                    Icon(Icons.Default.OpenInNew, contentDescription = null, tint = Gris600, modifier = Modifier.size(20.dp))
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
