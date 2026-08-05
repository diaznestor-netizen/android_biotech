package com.biobox.biotech.presentation.projects

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biobox.biotech.core.common.SyncStatus
import com.biobox.biotech.domain.model.Project
import com.biobox.biotech.domain.model.ProjectPriority
import com.biobox.biotech.domain.model.ProjectStatus
import com.biobox.biotech.presentation.components.loading.BioTechPullToRefresh
import com.biobox.biotech.presentation.components.textfields.BioTechSearchBar
import com.biobox.biotech.presentation.components.dialogs.BioTechConfirmationDialog
import com.biobox.biotech.presentation.components.states.EmptyState
import com.biobox.biotech.presentation.components.states.ErrorState
import com.biobox.biotech.presentation.components.indicators.StatusBadge
import com.biobox.biotech.presentation.components.cards.BioTechCard
import com.biobox.biotech.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProjectListScreen(
    onProjectClick: (String) -> Unit,
    onCreateProject: () -> Unit,
    onEditProject: (String) -> Unit,
    viewModel: ProjectViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateProject,
                containerColor = PrimaryGreen,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo proyecto")
            }
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ProjectHeader(state = state, viewModel = viewModel)

            BioTechPullToRefresh(
                isRefreshing = state.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.weight(1f)
            ) {
                AnimatedContent(
                    targetState = state,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
                    },
                    label = "project_list_state"
                ) { targetState ->
                    when {
                        targetState.isLoading && targetState.projects.isEmpty() -> {
                            ProjectSkeleton()
                        }

                        targetState.error != null && targetState.projects.isEmpty() -> {
                            ErrorState(
                                message = targetState.error ?: "No se pudo cargar proyectos",
                                onRetry = viewModel::refresh
                            )
                        }

                        targetState.visibleProjects.isEmpty() -> {
                            EmptyState(
                                title = "Sin proyectos visibles",
                                description = "Ajusta la búsqueda, cambia los filtros o crea un proyecto local."
                            )
                        }

                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                itemsIndexed(targetState.visibleProjects, key = { _, it -> it.localId }) { index, project ->
                                    var itemVisible by remember { mutableStateOf(false) }
                                    LaunchedEffect(Unit) {
                                        delay(index * 50L)
                                        itemVisible = true
                                    }
                                    
                                    AnimatedVisibility(
                                        visible = itemVisible,
                                        enter = fadeIn(tween(400)) + slideInHorizontally(tween(400)) { it / 2 },
                                        label = "project_item_anim"
                                    ) {
                                        ProjectItem(
                                            project = project,
                                            onOpen = { onProjectClick(project.localId) },
                                            onEdit = { onEditProject(project.localId) },
                                            onDelete = { viewModel.confirmDelete(project) },
                                            onRetrySync = { viewModel.retrySync(project) },
                                            onResolveConflict = { viewModel.askConflictResolution(project) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    state.projectPendingDeletion?.let {
        BioTechConfirmationDialog(
            title = "Eliminar proyecto",
            message = "La eliminación se registrará localmente y se sincronizará cuando la sesión tenga conexión.",
            onConfirm = viewModel::deleteSelectedProject,
            onDismiss = viewModel::dismissDeleteDialog,
            confirmText = "ELIMINAR",
            isDestructive = true
        )
    }

    state.projectPendingConflictResolution?.let {
        BioTechConfirmationDialog(
            title = "Resolver conflicto",
            message = "Puedes conservar la versión del servidor o volver a intentar la versión local sin sobrescribir silenciosamente los datos remotos.",
            onConfirm = { viewModel.resolveConflict(useRemote = true) },
            onDismiss = viewModel::dismissConflictDialog,
            confirmText = "USAR SERVIDOR",
            dismissText = "CANCELAR"
        )
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun ProjectHeader(
    state: ProjectListUiState,
    viewModel: ProjectViewModel
) {
    Column(modifier = Modifier.padding(16.dp)) {
        BioTechCard(
            containerColor = PrimaryGreen,
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(Brush.horizontalGradient(listOf(DarkSurface, DarkCard)))
                    .padding(20.dp)
            ) {
                Text(
                    text = "PROYECTOS OFFLINE-FIRST",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Gestión local con sincronización inteligente y resolución de conflictos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.84f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AssistChip(
                        onClick = {},
                        label = { Text("${state.projects.size} Proyectos") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color.White.copy(alpha = 0.15f),
                            labelColor = Color.White
                        )
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text("${state.pendingSyncCount} Pendientes") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Warning.copy(alpha = 0.2f),
                            labelColor = Color.White
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        BioTechSearchBar(
            query = state.searchQuery,
            onQueryChange = viewModel::onSearchQueryChange,
            placeholder = "Buscar por código, nombre o cliente..."
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "ESTADO OPERATIVO",
            style = MaterialTheme.typography.labelLarge,
            color = TextSecondaryDark,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = state.statusFilter == null,
                onClick = { viewModel.onStatusFilterChange(null) },
                label = { Text("TODOS") }
            )
            ProjectStatus.entries.forEach { status ->
                FilterChip(
                    selected = state.statusFilter == status,
                    onClick = { viewModel.onStatusFilterChange(status) },
                    label = { Text(status.name.replace('_', ' ')) }
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun ProjectItem(
    project: Project,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onRetrySync: () -> Unit,
    onResolveConflict: () -> Unit
) {
    val accent = when (project.prioridad) {
        ProjectPriority.CRITICA -> Error
        ProjectPriority.ALTA -> Warning
        ProjectPriority.MEDIA -> PrimaryBlue
        ProjectPriority.BAJA -> PrimaryGreen
    }

    BioTechCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        elevation = 2.dp,
        onClick = onOpen
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = accent.copy(alpha = 0.12f)
                ) {
                    Icon(
                        Icons.Default.Inventory2,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        project.codigo,
                        style = MaterialTheme.typography.labelLarge,
                        color = accent,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        project.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(
                        onClick = onOpen,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("VER DETALLE", color = PrimaryCyan, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    "${project.porcentajeAvance}%",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(status = project.syncStatus)
                Text(
                    "v${project.version}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            project.descripcion?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gris600,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            FlowRow(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoChip(Icons.Default.Flag, project.estado.name.replace('_', ' '))
                InfoChip(Icons.Default.Inventory2, project.cliente ?: "Sin cliente")
                InfoChip(Icons.Default.Warning, project.prioridad.name)
            }

            if (!project.responsableNombre.isNullOrBlank()) {
                Text(
                    text = "Responsable: ${project.responsableNombre}",
                    style = MaterialTheme.typography.labelMedium,
                    color = Gris600,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            project.conflictPayloadJson?.takeIf { project.syncStatus == SyncStatus.CONFLICT }?.let {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    color = Rojo.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Existe una diferencia entre la versión local y la versión remota. La resolución manual queda pendiente.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Rojo,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Editar")
                }
                TextButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Eliminar")
                }
                if (project.syncStatus == SyncStatus.FAILED) {
                    TextButton(onClick = onRetrySync) {
                        Icon(Icons.Default.CloudSync, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reintentar")
                    }
                }
                if (project.syncStatus == SyncStatus.CONFLICT) {
                    TextButton(onClick = onResolveConflict) {
                        Icon(Icons.Default.Warning, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Resolver conflicto")
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = AzulOscuro)
            Spacer(modifier = Modifier.width(6.dp))
            Text(value, style = MaterialTheme.typography.labelMedium, color = AzulOscuro)
        }
    }
}


@Composable
private fun ProjectSkeleton() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(5) {
            Card(
                modifier = Modifier.fillMaxWidth().height(170.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(modifier = Modifier.fillMaxSize())
            }
        }
    }
}


