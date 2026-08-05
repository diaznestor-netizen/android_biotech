package com.biobox.biotech.presentation.materials

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.biobox.biotech.presentation.components.loading.BioTechPullToRefresh
import com.biobox.biotech.presentation.components.loading.SkeletonLoader
import com.biobox.biotech.presentation.components.textfields.BioTechSearchBar
import com.biobox.biotech.presentation.components.states.EmptyState
import com.biobox.biotech.presentation.components.states.ErrorState
import com.biobox.biotech.presentation.components.indicators.StatusBadge
import com.biobox.biotech.presentation.components.cards.BioTechCard
import com.biobox.biotech.domain.model.Material
import com.biobox.biotech.presentation.theme.*

@Composable
fun MaterialListScreen(
    viewModel: MaterialViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "INVENTARIO DE MATERIALES",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            BioTechSearchBar(
                query = state.searchQuery,
                onQueryChange = { viewModel.onSearchQueryChange(it) },
                placeholder = "Buscar por código o nombre..."
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
                    label = { Text("TODOS") }
                )
                listOf("Disponible", "Stock bajo", "Agotado", "Crítico").forEach { status ->
                    FilterChip(
                        selected = state.statusFilter == status,
                        onClick = { viewModel.onStatusFilterChange(status) },
                        label = { Text(status.uppercase()) }
                    )
                }
            }
        }

        BioTechPullToRefresh(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.weight(1f)
        ) {
            if (state.isLoading && state.materials.isEmpty()) {
                MaterialListSkeleton()
            } else if (state.error != null && state.materials.isEmpty()) {
                ErrorState(message = state.error!!, onRetry = { viewModel.refresh() })
            } else if (state.materials.isEmpty()) {
                EmptyState(title = "Sin materiales", description = "No se encontraron materiales para esta búsqueda.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    itemsIndexed(state.materials, key = { _, it -> it.id ?: 0 }) { index, material ->
                        var itemVisible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            delay(index * 40L)
                            itemVisible = true
                        }
                        
                        AnimatedVisibility(
                            visible = itemVisible,
                            enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 3 },
                            label = "material_item_anim"
                        ) {
                            MaterialItem(material)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MaterialListSkeleton() {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(6) {
            SkeletonLoader(modifier = Modifier.fillMaxWidth(), height = 100.dp, shape = RoundedCornerShape(20.dp))
        }
    }
}

@Composable
fun MaterialItem(material: Material) {
    val computedStatus = when {
        material.estado.isNotBlank() && material.estado != "Disponible" -> material.estado
        material.cantidadDisponible <= 0 && material.stockMin > 0 -> "Crítico"
        material.cantidadDisponible <= 0 -> "Agotado"
        material.stockMin > 0 && material.cantidadDisponible <= material.stockMin -> "Stock bajo"
        else -> "Disponible"
    }

    val statusColor = when (computedStatus) {
        "Disponible" -> Success
        "Crítico" -> Error
        "Agotado" -> Error
        "Stock bajo" -> Warning
        else -> Warning
    }

    BioTechCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = material.codigo?.takeIf { it.isNotBlank() } ?: "SIN-CÓDIGO",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                    StatusBadge(status = material.syncStatus)
                }
                Text(
                    text = material.nombre?.takeIf { it.isNotBlank() } ?: "Material sin nombre",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (material.cantidadRequerida > 0) {
                        "STOCK: ${material.cantidadDisponible} / REQ: ${material.cantidadRequerida}"
                    } else {
                        "UNIDAD: ${material.unidad ?: "N/A"}  |  MIN: ${material.stockMin}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                material.descripcion?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Surface(
                color = statusColor.copy(alpha = 0.12f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = computedStatus.uppercase(),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    color = statusColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
