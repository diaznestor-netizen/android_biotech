package com.biobox.biotech.presentation.help

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.biobox.biotech.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpCenterScreen(
    onNavigateToLegal: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Centro de ayuda", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulOscuro, titleContentColor = Blanco)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GrisCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MenuBook, contentDescription = null, tint = AzulOscuro, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Manual de usuario", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AzulOscuro)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Consulta la guía completa de uso de BioTech", fontSize = 12.sp, color = Gris600)
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GrisCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.QuestionAnswer, contentDescription = null, tint = AzulOscuro, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Preguntas frecuentes", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AzulOscuro)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Respuestas a las dudas más comunes", fontSize = 12.sp, color = Gris600)
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GrisCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ContactSupport, contentDescription = null, tint = AzulOscuro, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Soporte técnico", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AzulOscuro)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("soporte@biotech.grupovallas.com", fontSize = 12.sp, color = Gris600)
                    }
                }
            }

            item {
                Card(
                    onClick = onNavigateToLegal,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GrisCard)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = AzulOscuro, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Derechos, licencias y políticas", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = AzulOscuro)
                            Text("Copyright, licencias, términos y privacidad", fontSize = 12.sp, color = Gris600)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Gris600)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text("BioTech v1.0.0", fontSize = 12.sp, color = Gris500, modifier = Modifier.fillMaxWidth())
                Text("BioBox - Grupo Vallas", fontSize = 12.sp, color = Gris500, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
