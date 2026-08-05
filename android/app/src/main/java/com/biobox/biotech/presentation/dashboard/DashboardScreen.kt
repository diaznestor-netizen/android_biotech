package com.biobox.biotech.presentation.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.biobox.biotech.core.common.UiState
import com.biobox.biotech.presentation.common.SyncStatusViewModel
import com.biobox.biotech.presentation.components.loading.SkeletonLoader
import com.biobox.biotech.presentation.components.loading.biotechShimmer
import com.biobox.biotech.presentation.components.states.ErrorState
import com.biobox.biotech.presentation.components.indicators.OfflineBanner
import com.biobox.biotech.presentation.components.cards.BioTechCard
import com.biobox.biotech.presentation.components.cards.KPIWidget
import com.biobox.biotech.presentation.theme.*

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    syncViewModel: SyncStatusViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val syncState by syncViewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        OfflineBanner(visible = !syncState.isServerConnected)
        
        AnimatedContent(
            targetState = uiState,
            transitionSpec = {
                fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
            },
            label = "dashboard_state"
        ) { state ->
            when (state) {
                is UiState.Loading -> {
                    DashboardSkeleton()
                }

                is UiState.Error -> {
                    ErrorState(
                        message = state.message,
                        onRetry = { viewModel.refresh() }
                    )
                }

                is UiState.Success -> {
                    val data = state.data
                    val stats = listOf(
                        StatItem("Máquinas registradas", data.totalMachines.toString(), Icons.Default.PrecisionManufacturing, PrimaryBlue),
                        StatItem("Máquinas activas", data.completedMachines.toString(), Icons.Default.CheckCircle, PrimaryGreen),
                        StatItem("Pendientes", data.incompleteMachines.toString(), Icons.Default.Warning, Warning),
                        StatItem("Progreso general", "${data.averageProgress}%", Icons.Default.Analytics, PrimaryCyan)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            HeroSummaryCard(
                                totalMachines = data.totalMachines,
                                completedMachines = data.completedMachines,
                                averageProgress = data.averageProgress
                            )
                        }

                        item {
                            ConnectionStatusCard(syncState, onRefresh = { 
                                viewModel.refresh()
                                syncViewModel.triggerSync()
                            })
                        }

                        item {
                            Text(
                                text = "PANEL OPERATIVO",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }

                        item {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                userScrollEnabled = false,
                                modifier = Modifier.height(280.dp)
                            ) {
                        items(stats) { item ->
                            KPIWidget(
                                label = item.label,
                                value = item.value,
                                icon = item.icon,
                                color = item.color
                            )
                        }
                            }
                        }
                    }
                }
                else -> Unit
            }
        }
    }
}

@Composable
private fun DashboardSkeleton() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SkeletonLoader(modifier = Modifier.fillMaxWidth(), height = 180.dp, shape = RoundedCornerShape(28.dp))
        }
        item { SkeletonLoader(width = 150.dp, height = 24.dp) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                SkeletonLoader(modifier = Modifier.weight(1f), height = 120.dp, shape = RoundedCornerShape(24.dp))
                SkeletonLoader(modifier = Modifier.weight(1f), height = 120.dp, shape = RoundedCornerShape(24.dp))
            }
        }
    }
}

@Composable
private fun ConnectionStatusCard(
    state: com.biobox.biotech.presentation.common.SyncStatusState,
    onRefresh: () -> Unit
) {
    BioTechCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        elevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = if (state.isServerConnected) PrimaryGreen else Gris500,
                        modifier = Modifier.size(8.dp)
                    ) {}
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (state.isServerConnected) "SERVIDOR EN LÍNEA" else "MODO OFFLINE",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (state.isServerConnected) PrimaryGreen else TextSecondaryDark
                    )
                }
                Text(
                    text = "Última sincronización: ${state.lastSyncTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (state.pendingOperationsCount > 0) {
                    Text(
                        text = "${state.pendingOperationsCount} cambios pendientes",
                        style = MaterialTheme.typography.bodySmall,
                        color = Warning,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            IconButton(onClick = onRefresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = PrimaryCyan)
            }
        }
    }
}

@Composable
private fun HeroSummaryCard(
    totalMachines: Int,
    completedMachines: Int,
    averageProgress: Int
) {
    BioTechCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = PrimaryBlue,
        elevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(PrimaryBlue, PrimaryCyan.copy(alpha = 0.7f))
                    )
                )
                .padding(20.dp)
        ) {
            Text(
                text = "OPERACIÓN CENTRAL",
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "$averageProgress%",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "AVANCE PROMEDIO GLOBAL",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CompactMetricChip(
                    label = "REGISTRADAS",
                    value = totalMachines.toString(),
                    modifier = Modifier.weight(1f)
                )
                CompactMetricChip(
                    label = "ACTIVAS",
                    value = completedMachines.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CompactMetricChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.15f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun StatCard(item: StatItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = item.color.copy(alpha = 0.12f)
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.color,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(24.dp)
                )
            }
            Text(
                text = item.value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = item.label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

data class StatItem(val label: String, val value: String, val icon: ImageVector, val color: Color)
