package com.biobox.biotech.presentation.audit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.biobox.biotech.core.common.SyncStatus
import com.biobox.biotech.presentation.components.cards.BioTechCard
import com.biobox.biotech.presentation.components.indicators.StatusBadge
import com.biobox.biotech.presentation.components.navigation.BioTechTopBar
import com.biobox.biotech.presentation.components.textfields.BioTechSearchBar
import com.biobox.biotech.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditLogScreen() {
    var searchQuery by remember { mutableStateOf("") }
    
    // Dummy audit logs for Phase 4 visualization
    val auditLogs = remember {
        listOf(
            AuditEntry("Usuario: Admin", "Cambio en configuración de máquina M-452", "15:45 - 26 Jul 2026", SyncStatus.SYNCED, Icons.Default.Settings),
            AuditEntry("Usuario: Operario 1", "Inicio de mantenimiento preventivo P-102", "14:20 - 26 Jul 2026", SyncStatus.PENDING, Icons.Default.Build),
            AuditEntry("Usuario: Supervisor", "Aprobación de reporte de inventario mensual", "12:10 - 26 Jul 2026", SyncStatus.SYNCED, Icons.Default.AssignmentTurnedIn),
            AuditEntry("Usuario: Sistema", "Error de sincronización en módulo de materiales", "09:30 - 26 Jul 2026", SyncStatus.FAILED, Icons.Default.SyncProblem),
            AuditEntry("Usuario: Admin", "Nuevo usuario registrado: Nestor Gaona", "08:15 - 26 Jul 2026", SyncStatus.SYNCED, Icons.Default.PersonAdd)
        )
    }

    Scaffold(
        topBar = {
            BioTechTopBar(title = "AUDITORÍA Y TRAZABILIDAD")
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                BioTechSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Filtrar por usuario, acción o fecha..."
                )
            }

            Text(
                text = "LÍNEA DE TIEMPO OPERATIVA",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondaryDark,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                letterSpacing = 1.sp
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(auditLogs) { entry ->
                    AuditTimelineItem(entry)
                }
            }
        }
    }
}

@Composable
private fun AuditTimelineItem(entry: AuditEntry) {
    val statusColor = when (entry.status) {
        SyncStatus.SYNCED -> Success
        SyncStatus.PENDING -> Warning
        SyncStatus.FAILED -> Error
        else -> PrimaryCyan
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Timeline connector
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Surface(
                modifier = Modifier.size(12.dp),
                shape = CircleShape,
                color = statusColor,
                border = androidx.compose.foundation.BorderStroke(2.dp, Color.White.copy(alpha = 0.5f))
            ) {}
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(2.dp)
                    .background(Color.White.copy(alpha = 0.1f))
            )
        }

        // Audit Card
        BioTechCard(
            modifier = Modifier
                .padding(bottom = 16.dp)
                .fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface,
            elevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.1f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = entry.icon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = entry.user,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryCyan
                        )
                        StatusBadge(status = entry.status)
                    }
                    Text(
                        text = entry.action,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = entry.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondaryDark,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

data class AuditEntry(
    val user: String,
    val action: String,
    val timestamp: String,
    val status: SyncStatus,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
