package com.biobox.biotech.presentation.components.indicators

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.biobox.biotech.core.common.SyncStatus
import com.biobox.biotech.presentation.theme.*

@Composable
fun OfflineBanner(visible: Boolean) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        Surface(
            color = Warning,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.CloudOff, contentDescription = null, tint = Blanco, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "TRABAJANDO EN MODO LOCAL (SIN CONEXIÓN)",
                    color = Blanco,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun GlobalSyncStatusBar(
    state: com.biobox.biotech.presentation.common.SyncStatusState,
    modifier: Modifier = Modifier
) {
    val (bgColor, statusText) = when {
        state.errorCount > 0 -> Error to "Errores Detectados"
        state.conflictCount > 0 -> Warning to "Conflictos de Sincronización"
        state.isSyncing -> Info to "Sincronizando..."
        !state.isServerConnected -> Gris600 to "Modo Offline"
        else -> AzulMedio to "Servidor Conectado"
    }

    Surface(
        color = bgColor,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (state.isServerConnected) Icons.Default.CloudQueue else Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = Blanco,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = statusText,
                    color = Blanco,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (state.pendingOperationsCount > 0 || state.errorCount > 0 || state.conflictCount > 0) {
                    SyncMetric(Icons.Default.PendingActions, state.pendingOperationsCount, Blanco)
                    if (state.errorCount > 0) {
                        Spacer(modifier = Modifier.width(12.dp))
                        SyncMetric(Icons.Default.ErrorOutline, state.errorCount, Blanco)
                    }
                    if (state.conflictCount > 0) {
                        Spacer(modifier = Modifier.width(12.dp))
                        SyncMetric(Icons.Default.Warning, state.conflictCount, Blanco)
                    }
                } else {
                    Text(
                        text = "Sincronizado: ${state.lastSyncTime}",
                        color = Blanco.copy(alpha = 0.8f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SyncMetric(icon: ImageVector, count: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(12.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = count.toString(), color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StatusBadge(
    status: SyncStatus,
    modifier: Modifier = Modifier
) {
    val (icon, color, text) = when (status) {
        SyncStatus.SYNCED -> Triple(Icons.Default.CheckCircle, Success, "Sincronizado")
        SyncStatus.PENDING -> Triple(Icons.Default.Schedule, Warning, "Pendiente")
        SyncStatus.SYNCING -> Triple(Icons.Default.Sync, Info, "Sincronizando...")
        SyncStatus.FAILED -> Triple(Icons.Default.Error, Error, "Error")
        SyncStatus.CONFLICT -> Triple(Icons.Default.Warning, Error, "Conflicto")
    }

    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text.uppercase(),
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        }
    }
}
