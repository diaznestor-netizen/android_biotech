package com.biobox.biotech.presentation.machines

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.biobox.biotech.domain.model.Component
import com.biobox.biotech.domain.model.MachineStatus
import com.biobox.biotech.presentation.components.cards.BioTechCard
import com.biobox.biotech.presentation.components.navigation.BioTechTopBar
import com.biobox.biotech.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MachineDetailScreen(
    onBack: () -> Unit,
    onStartInspection: (Int) -> Unit,
    viewModel: MachineDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val machine = state.machine

    Scaffold(
        topBar = { BioTechTopBar("DETALLE DE PRODUCCIÓN", navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Regresar") }
        }) },
        containerColor = DarkBackground
    ) { padding ->
        if (state.isLoading && machine == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryCyan)
            }
        } else if (machine == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(state.error ?: "No fue posible cargar la máquina.", color = Error)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    BioTechCard(modifier = Modifier.fillMaxWidth(), containerColor = DarkSurface, elevation = 8.dp) {
                        Column(Modifier.padding(20.dp)) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                Text(machine.codigo, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = PrimaryCyan)
                                Text(machine.estado.name.replace('_', ' '), color = if (machine.estado == MachineStatus.COMPLETA) Success else Warning)
                            }
                            Text(machine.nombre, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Text("ÁREA: " + machine.area.uppercase(), color = TextSecondaryDark)
                            Text("RESPONSABLE: " + (machine.responsable ?: "SIN ASIGNAR"), color = TextSecondaryDark)
                            Spacer(Modifier.height(16.dp))
                            val progress = state.completion?.progress ?: machine.porcentajeAvance.toDouble()
                            Text("AVANCE DE ENSAMBLAJE: " + progress.toInt() + "%", fontWeight = FontWeight.Bold, color = PrimaryCyan)
                            LinearProgressIndicator(
                                progress = { (progress / 100.0).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                                color = PrimaryCyan
                            )
                        }
                    }
                }

                state.completion?.let { completion ->
                    item {
                        val missing = completion.missingComponents + completion.missingEvidence + completion.missingMaterialIds.map { "Material #" + it }
                        Text(
                            if (completion.canBeFinished) "LISTA PARA FINALIZAR"
                            else "PENDIENTES: " + missing.joinToString(", ").ifBlank { "validaciones de calidad" },
                            color = if (completion.canBeFinished) Success else Warning,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                item { Text("COMPONENTES", fontWeight = FontWeight.Bold, color = TextSecondaryDark, letterSpacing = 1.sp) }

                items(state.components.ifEmpty { machine.componentes }, key = { it.id }) { component ->
                    ComponentProductionItem(component, state.saving) { newState ->
                        viewModel.updateComponent(component.id, newState)
                    }
                }

                item {
                    Button(
                        onClick = { onStartInspection(machine.id) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Icon(Icons.Default.FactCheck, contentDescription = null)
                        Spacer(Modifier.width(12.dp))
                        Text("ABRIR INSPECCIÓN", fontWeight = FontWeight.Bold)
                    }
                }

                item {
                    if (state.error != null) Text(state.error!!, color = Error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ComponentProductionItem(component: Component, saving: Boolean, onStateChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    BioTechCard(modifier = Modifier.fillMaxWidth(), containerColor = MaterialTheme.colorScheme.surface) {
        Column(Modifier.padding(16.dp)) {
            Text(component.nombre.uppercase(), fontWeight = FontWeight.Bold, color = PrimaryCyan)
            Spacer(Modifier.height(8.dp))
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = component.estado,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Estado") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    enabled = !saving
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    listOf("PENDIENTE", "INSTALADO", "DANADO", "REEMPLAZADO").forEach { value ->
                        DropdownMenuItem(text = { Text(value) }, onClick = {
                            expanded = false
                            onStateChange(value)
                        })
                    }
                }
            }
        }
    }
}