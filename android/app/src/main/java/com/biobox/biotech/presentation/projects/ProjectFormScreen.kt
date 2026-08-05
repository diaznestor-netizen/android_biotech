package com.biobox.biotech.presentation.projects

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biobox.biotech.domain.model.ProjectPriority
import com.biobox.biotech.domain.model.ProjectStatus
import com.biobox.biotech.presentation.components.dialogs.BioTechConfirmationDialog
import com.biobox.biotech.presentation.components.states.ErrorState
import com.biobox.biotech.presentation.components.navigation.BioTechTopBar
import com.biobox.biotech.presentation.components.textfields.BioTechTextField
import com.biobox.biotech.presentation.components.cards.BioTechCard
import com.biobox.biotech.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProjectFormScreen(
    onBack: () -> Unit,
    viewModel: ProjectFormViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showDiscard by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect {
            onBack()
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
                title = when {
                    state.conflict.isMergeMode -> "COMBINAR CONFLICTO"
                    state.isEditMode -> "EDITAR PROYECTO"
                    else -> "NUEVO PROYECTO"
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.hasUnsavedChanges && !state.form.isSaving) showDiscard = true else onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        when {
            state.error != null && state.isEditMode && state.form.codigo.isBlank() -> ErrorState(message = state.error ?: "No se pudo cargar", onRetry = onBack)
            else -> {
                val form = state.form
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    BioTechCard(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(
                                "DATOS OPERATIVOS",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue,
                                letterSpacing = 1.sp
                            )
                            
                            if (state.hasUnsavedChanges) {
                                Surface(
                                    color = Warning.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        "CAMBIOS SIN GUARDAR",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        color = Warning,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (state.conflict.isMergeMode) {
                                ConflictMergeSection(
                                    conflict = state.conflict,
                                    onUseRemote = viewModel::useRemoteValue,
                                    onUseLocal = viewModel::useLocalValue
                                )
                            }

                            BioTechTextField(
                                value = form.codigo,
                                onValueChange = viewModel::onCodigoChange,
                                label = "Código del Proyecto",
                                leadingIcon = Icons.Default.QrCode,
                                isError = form.fieldErrors.containsKey("codigo"),
                                errorText = form.fieldErrors["codigo"]
                            )

                            BioTechTextField(
                                value = form.nombre,
                                onValueChange = viewModel::onNombreChange,
                                label = "Nombre del Proyecto",
                                leadingIcon = Icons.AutoMirrored.Filled.Assignment,
                                isError = form.fieldErrors.containsKey("nombre"),
                                errorText = form.fieldErrors["nombre"]
                            )

                            BioTechTextField(
                                value = form.descripcion,
                                onValueChange = viewModel::onDescripcionChange,
                                label = "Descripción",
                                leadingIcon = Icons.Default.Description,
                                singleLine = false
                            )

                            BioTechTextField(
                                value = form.cliente,
                                onValueChange = viewModel::onClienteChange,
                                label = "Cliente",
                                leadingIcon = Icons.Default.Business
                            )

                            BioTechTextField(
                                value = form.responsableNombre,
                                onValueChange = viewModel::onResponsableChange,
                                label = "Responsable",
                                leadingIcon = Icons.Default.Person
                            )

                            DateField("Fecha inicio", form.fechaInicio, form.fieldErrors["fechaInicio"]) {
                                openDatePicker(context, form.fechaInicio, viewModel::onFechaInicioChange)
                            }
                            
                            DateField("Fecha fin estimada", form.fechaFinEstimada, form.fieldErrors["fechaFinEstimada"]) {
                                openDatePicker(context, form.fechaFinEstimada, viewModel::onFechaFinEstimadaChange)
                            }

                            BioTechTextField(
                                value = form.porcentajeAvance,
                                onValueChange = viewModel::onAvanceChange,
                                label = "Avance (%)",
                                leadingIcon = Icons.AutoMirrored.Filled.TrendingUp,
                                isError = form.fieldErrors.containsKey("porcentajeAvance"),
                                errorText = form.fieldErrors["porcentajeAvance"]
                            )

                            Text("ESTADO ACTUAL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextSecondaryDark)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                ProjectStatus.entries.forEach { status ->
                                    FilterChip(
                                        selected = form.estado == status,
                                        onClick = { viewModel.onEstadoChange(status) },
                                        label = { Text(status.name.replace('_', ' ')) }
                                    )
                                }
                            }

                            Text("PRIORIDAD", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = TextSecondaryDark)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                ProjectPriority.entries.forEach { priority ->
                                    FilterChip(
                                        selected = form.prioridad == priority,
                                        onClick = { viewModel.onPrioridadChange(priority) },
                                        label = { Text(priority.name) }
                                    )
                                }
                            }

                            form.error?.let { Text(it, color = Error, style = MaterialTheme.typography.bodySmall) }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(
                                onClick = viewModel::save,
                                enabled = !form.isSaving,
                                modifier = Modifier.fillMaxWidth().height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                            ) {
                                if (form.isSaving) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                                } else {
                                    Text("GUARDAR PROYECTO", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDiscard && !state.form.isSaving) {
        BioTechConfirmationDialog(
            title = "Cambios sin guardar",
            message = "Si vuelves ahora, perderás los cambios del formulario.",
            onConfirm = {
                showDiscard = false
                onBack()
            },
            onDismiss = { showDiscard = false },
            confirmText = "DESCARTAR"
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ConflictMergeSection(
    conflict: ProjectFormConflictUiState,
    onUseRemote: (String) -> Unit,
    onUseLocal: (String) -> Unit
) {
    BioTechCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = Error.copy(alpha = 0.05f)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("COMPARACIÓN DE CONFLICTO", style = MaterialTheme.typography.titleSmall, color = Error, fontWeight = FontWeight.Bold)
            Text(
                "Versión remota: ${conflict.remoteVersion ?: "N/A"}",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondaryDark
            )
            conflict.differences.forEach { diff ->
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(diff.field.uppercase(), color = PrimaryBlue, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text("LOCAL: ${diff.localValue}", style = MaterialTheme.typography.bodySmall, color = TextSecondaryDark)
                    Text("SERVIDOR: ${diff.remoteValue}", style = MaterialTheme.typography.bodySmall, color = Error, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { onUseLocal(diff.field) }) {
                            Text("USAR LOCAL", color = PrimaryCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        TextButton(onClick = { onUseRemote(diff.field) }) {
                            Text("USAR SERVIDOR", color = Error, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DateField(
    label: String,
    value: Long?,
    error: String?,
    onClick: () -> Unit
) {
    BioTechTextField(
        value = formatProjectDate(value),
        onValueChange = {},
        label = label,
        modifier = Modifier.clickable(onClick = onClick),
        leadingIcon = Icons.Default.CalendarToday,
        isError = error != null,
        errorText = error
    )
}

private fun openDatePicker(
    context: android.content.Context,
    current: Long?,
    onDateSelected: (Long) -> Unit
) {
    val calendar = java.util.Calendar.getInstance().apply {
        timeInMillis = current ?: System.currentTimeMillis()
    }
    DatePickerDialog(
        context,
        { _, year, month, day ->
            val selected = java.util.Calendar.getInstance().apply {
                set(year, month, day, 0, 0, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            onDateSelected(selected.timeInMillis)
        },
        calendar.get(java.util.Calendar.YEAR),
        calendar.get(java.util.Calendar.MONTH),
        calendar.get(java.util.Calendar.DAY_OF_MONTH)
    ).show()
}
