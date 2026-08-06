package com.biobox.biotech.presentation.peru

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
import com.biobox.biotech.presentation.dashboard.DashboardViewModel
import com.biobox.biotech.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeruMachinesScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Máquinas Perú", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulOscuro, titleContentColor = Blanco)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(64.dp), tint = Rojo)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Máquinas destinadas a Perú", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AzulOscuro)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Control de fabricación, instalación y puesta en operación", fontSize = 13.sp, color = Gris600)
            }
        }
    }
}
