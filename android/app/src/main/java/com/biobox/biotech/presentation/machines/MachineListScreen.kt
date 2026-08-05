package com.biobox.biotech.presentation.machines

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.biobox.biotech.presentation.components.loading.BioTechPullToRefresh
import com.biobox.biotech.presentation.components.textfields.BioTechSearchBar
import com.biobox.biotech.presentation.components.loading.SkeletonLoader
import com.biobox.biotech.presentation.components.loading.biotechShimmer
import com.biobox.biotech.presentation.components.states.EmptyState
import com.biobox.biotech.presentation.components.states.ErrorState
import com.biobox.biotech.presentation.components.indicators.StatusBadge
import com.biobox.biotech.presentation.components.cards.BioTechCard
import com.biobox.biotech.domain.model.Machine
import com.biobox.biotech.domain.model.MachineStatus
import com.biobox.biotech.presentation.theme.*

@Composable
fun MachineListScreen(
    onMachineClick: (Int) -> Unit,
    viewModel: MachineViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "MAQUINARIA INDUSTRIAL",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            BioTechSearchBar(
                query = state.searchQuery,
                onQueryChange = { viewModel.onSearchQueryChange(it) },
                placeholder = "Buscar máquina por nombre o código..."
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.statusFilter == null,
                    onClick = { viewModel.onStatusFilterChange(null) },
                    label = { Text("TODAS") }
                )
                MachineStatus.values().forEach { status ->
                    FilterChip(
                        selected = state.statusFilter == status,
                        onClick = { viewModel.onStatusFilterChange(status) },
                        label = { Text(status.name.replace('_', ' ')) }
                    )
                }
            }
        }

        BioTechPullToRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.weight(1f)
        ) {
            AnimatedContent(
                targetState = state,
                transitionSpec = {
                    fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
                },
                label = "machine_list_state"
            ) { targetState ->
                if (targetState.isLoading && targetState.machines.isEmpty()) {
                    MachineListSkeleton()
                } else if (targetState.error != null && targetState.machines.isEmpty()) {
                    ErrorState(message = targetState.error!!, onRetry = { viewModel.refresh() })
                } else if (targetState.machines.isEmpty()) {
                    EmptyState(title = "No se encontraron máquinas")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        itemsIndexed(targetState.machines, key = { _, it -> it.id }) { index, machine ->
                            var itemVisible by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) {
                                delay(index * 50L)
                                itemVisible = true
                            }
                            
                            AnimatedVisibility(
                                visible = itemVisible,
                                enter = fadeIn(tween(400)) + slideInHorizontally(tween(400)) { it / 2 },
                                label = "machine_item_anim"
                            ) {
                                MachineItem(machine, onMachineClick)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MachineListSkeleton() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(5) {
            SkeletonLoader(modifier = Modifier.fillMaxWidth(), height = 110.dp, shape = RoundedCornerShape(20.dp))
        }
    }
}

@Composable
fun MachineItem(machine: Machine, onClick: (Int) -> Unit) {
    val statusColor = when (machine.estado) {
        MachineStatus.COMPLETA -> PrimaryGreen
        MachineStatus.NO_OPERATIVA -> Error
        else -> Warning
    }

    BioTechCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        elevation = 2.dp,
        onClick = { onClick(machine.id) }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = machine.imagenUrl ?: Icons.Default.PrecisionManufacturing,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = machine.codigo,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    StatusBadge(status = machine.syncStatus) 
                }
                Text(
                    text = machine.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = machine.area,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = machine.porcentajeAvance / 100f,
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = statusColor,
                    trackColor = statusColor.copy(alpha = 0.1f)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = "${machine.porcentajeAvance}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
