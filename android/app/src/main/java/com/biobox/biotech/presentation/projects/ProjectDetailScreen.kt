package com.biobox.biotech.presentation.projects

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biobox.biotech.core.common.SyncStatus
import com.biobox.biotech.domain.model.Project
import com.biobox.biotech.presentation.components.dialogs.BioTechConfirmationDialog
import com.biobox.biotech.presentation.components.states.EmptyState
import com.biobox.biotech.presentation.components.states.ErrorState
import com.biobox.biotech.presentation.components.indicators.StatusBadge
import com.biobox.biotech.presentation.components.cards.BioTechCard
import com.biobox.biotech.presentation.components.navigation.BioTechTopBar
import com.biobox.biotech.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProjectDetailScreen(
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onMergeConflict: (String) -> Unit,
    viewModel: ProjectDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val conflictDetails = state.project?.let { project ->
        ProjectConflictComparator.parse(
            project.conflictPayloadJson,
            project.toPresentationSnapshot()
        )
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ProjectDetailEvent.NavigateBack -> onBack()
                is ProjectDetailEvent.NavigateToEdit -> onEdit(event.localId)
                is ProjectDetailEvent.NavigateToConflictMerge -> onMergeConflict(event.localId)
            }
        }
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            BioTechTopBar(
                title = "DETALLE DE PROYECTO",
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        when {
            state.isLoading -> Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryCyan)
            }
            state.error != null && state.project == null -> ErrorState(message = state.error ?: "No se pudo cargar", onRetry = viewModel::refresh)
            state.project == null -> EmptyState(title = "Proyecto no disponible", description = "El identificador local no existe en la base local.")
            else -> ProjectDetailContent(
                project = state.project!!,
                conflictDetails = conflictDetails,
                modifier = Modifier.padding(padding),
                onEdit = viewModel::editProject,
                onDelete = viewModel::askDelete
            )
        }
    }

    if (state.showDeleteDialog) {
        BioTechConfirmationDialog(
            title = "Eliminar proyecto",
            message = "Se registrará una eliminación lógica local y la lista se actualizará automáticamente.",
            onConfirm = viewModel::deleteProject,
            onDismiss = viewModel::dismissDelete,
            confirmText = "ELIMINAR",
            isDestructive = true
        )
    }

    if (state.showConflictDialog) {
        ConflictResolutionDialog(
            onAcceptServer = { viewModel.resolveConflict(useRemote = true) },
            onKeepLocal = { viewModel.resolveConflict(useRemote = false) },
            onMergeManually = viewModel::combineConflictManually,
            onDismiss = viewModel::dismissConflictResolution
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProjectDetailContent(
    project: Project,
    conflictDetails: ProjectConflictDetails?,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            BioTechCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        project.codigo,
                        style = MaterialTheme.typography.labelLarge,
                        color = PrimaryBlue,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        project.nombre,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold
                    )
                    StatusBadge(project.syncStatus)
                    
                    if (project.syncStatus == SyncStatus.CONFLICT) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            color = Error.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Warning, contentDescription = null, tint = Error, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "CONFLICTO DETECTADO: Revisa los cambios antes de decidir.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Error,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("EDITAR", fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Error),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ELIMINAR", fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ProjectInfoItem("CLIENTE", project.cliente ?: "N/A", Icons.Default.Business)
                ProjectInfoItem("RESPONSABLE", project.responsableNombre ?: "N/A", Icons.Default.Person)
                ProjectInfoItem("ESTADO", project.estado.name.replace('_', ' '), Icons.Default.Flag)
                ProjectInfoItem("PRIORIDAD", project.prioridad.name, Icons.Default.PriorityHigh)
                ProjectInfoItem("AVANCE", "${project.porcentajeAvance}%", Icons.AutoMirrored.Filled.TrendingUp)
            }
        }

        item {
            BioTechCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "DESCRIPCIÓN",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondaryDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        project.descripcion ?: "Sin descripción adicional.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        if (project.syncStatus == SyncStatus.CONFLICT && conflictDetails != null) {
            item {
                Text(
                    "DETALLE DE CONFLICTO",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = Error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(conflictDetails.differences) { diff ->
                BioTechCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Error.copy(alpha = 0.05f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(diff.field.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("LOCAL: ${diff.localValue}", style = MaterialTheme.typography.bodyMedium, color = TextSecondaryDark)
                        Text("SERVIDOR: ${diff.remoteValue}", style = MaterialTheme.typography.bodyMedium, color = Error, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectInfoItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    BioTechCard(modifier = Modifier.fillMaxWidth(), elevation = 0.dp) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = PrimaryCyan, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondaryDark, fontWeight = FontWeight.Bold)
                Text(value.uppercase(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun ConflictResolutionDialog(
    onAcceptServer: () -> Unit,
    onKeepLocal: () -> Unit,
    onMergeManually: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        BioTechCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = DarkSurface,
            shape = RoundedCornerShape(28.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("RESOLVER CONFLICTO", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    "Elige una estrategia controlada. No se sobrescribirá automáticamente ninguna versión.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondaryDark
                )
                Button(onClick = onAcceptServer, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)) {
                    Text("ACEPTAR VERSIÓN DEL SERVIDOR")
                }
                OutlinedButton(onClick = onKeepLocal, modifier = Modifier.fillMaxWidth(), border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryCyan)) {
                    Text("CONSERVAR VERSIÓN LOCAL", color = PrimaryCyan)
                }
                TextButton(onClick = onMergeManually, modifier = Modifier.fillMaxWidth()) {
                    Text("COMBINAR CAMBIOS MANUALMENTE", color = TextSecondaryDark)
                }
            }
        }
    }
}

private fun Project.toPresentationSnapshot(): ProjectPresentationSnapshot {
    return ProjectPresentationSnapshot(
        codigo = codigo,
        nombre = nombre,
        descripcion = descripcion.orEmpty(),
        cliente = cliente.orEmpty(),
        responsable = responsableNombre.orEmpty(),
        estado = estado.name,
        prioridad = prioridad.name,
        fechaInicio = formatProjectDate(fechaInicio),
        fechaFinEstimada = formatProjectDate(fechaFinEstimada),
        fechaFinReal = formatProjectDate(fechaFinReal),
        porcentajeAvance = porcentajeAvance.toString(),
        observaciones = observaciones.orEmpty()
    )
}
