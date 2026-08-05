package com.biobox.biotech.presentation.inspections

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.biobox.biotech.presentation.inspections.components.CameraPreview
import com.biobox.biotech.presentation.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InspectionFlowScreen(
    onBack: () -> Unit,
    onFinished: () -> Unit,
    viewModel: InspectionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    if (state.isSubmitted) {
        LaunchedEffect(Unit) { onFinished() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inspección de Máquina", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.currentStep == 0) {
                            onBack()
                        } else {
                            viewModel.prevStep()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AzulOscuro,
                    titleContentColor = Blanco,
                    navigationIconContentColor = Blanco
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            StepIndicator(currentStep = state.currentStep)
            
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                AnimatedContent(
                    targetState = state.currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                        } else {
                            slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                        }.using(SizeTransform(clip = false))
                    }, label = ""
                ) { step ->
                    when (step) {
                        0 -> SelectionStep(state.machine)
                        1 -> CheckStep(state.machine, state.items, viewModel::onQuantityChange)
                        2 -> EvidenceStep(state.capturedImages, viewModel::captureImage, viewModel::removeImage)
                        3 -> SummaryStep(state.machine, state.items, state.observaciones, viewModel::onObservacionesChange)
                    }
                }
            }

            BottomActionBar(
                currentStep = state.currentStep,
                isLoading = state.isLoading,
                onNext = { if (state.currentStep < 3) viewModel.nextStep() else viewModel.submitInspection() }
            )
        }
    }
}

@Composable
fun StepIndicator(currentStep: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val steps = listOf("Máquina", "Revisión", "Evidencias", "Resumen")
        steps.forEachIndexed { index, label ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(if (index <= currentStep) VerdePrincipal else Gris300, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (index < currentStep) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Blanco, modifier = Modifier.size(16.dp))
                    } else {
                        Text(text = (index + 1).toString(), color = Blanco, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = label, fontSize = 10.sp, color = if (index <= currentStep) AzulOscuro else Gris600)
            }
            if (index < steps.size - 1) {
                Divider(modifier = Modifier.weight(1f).padding(top = 16.dp), color = if (index < currentStep) VerdePrincipal else Gris300)
            }
        }
    }
}

@Composable
fun SelectionStep(machine: com.biobox.biotech.domain.model.Machine?) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.PrecisionManufacturing, contentDescription = null, modifier = Modifier.size(80.dp), tint = AzulOscuro)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Confirmar Máquina", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = GrisCard)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Código: ${machine?.codigo}", fontWeight = FontWeight.Bold)
                Text(text = "Nombre: ${machine?.nombre}", fontSize = 18.sp, color = AzulOscuro, fontWeight = FontWeight.Bold)
                Text(text = "Área: ${machine?.area}")
            }
        }
    }
}

@Composable
fun CheckStep(
    machine: com.biobox.biotech.domain.model.Machine?,
    items: Map<Int, Int>,
    onQuantityChange: (Int, Int) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        machine?.componentes?.forEach { componente ->
            item {
                Text(text = componente.nombre, fontWeight = FontWeight.ExtraBold, color = AzulOscuro, fontSize = 16.sp)
            }
            items(componente.materiales) { material ->
                val materialId = material.id
                if (materialId != null) {
                    InspectionItemRow(material, items[materialId] ?: 0, onQuantityChange)
                }
            }
        }
    }
}

@Composable
fun InspectionItemRow(
    material: com.biobox.biotech.domain.model.Material,
    quantity: Int,
    onQuantityChange: (Int, Int) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = GrisCard)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = material.nombre, fontWeight = FontWeight.Bold)
                Text(text = "Requerido: ${material.cantidadRequerida}", fontSize = 12.sp, color = Gris600)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { material.id?.let { if (quantity > 0) onQuantityChange(it, quantity - 1) } }) {
                    Icon(Icons.Default.Remove, contentDescription = null)
                }
                Text(text = quantity.toString(), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                IconButton(onClick = { material.id?.let { onQuantityChange(it, quantity + 1) } }) {
                    Icon(Icons.Default.Add, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun EvidenceStep(
    images: List<Uri>,
    onCapture: (ImageCapture) -> Unit,
    onRemove: (Uri) -> Unit
) {
    val imageCapture = remember { ImageCapture.Builder().build() }
    var hasPermission by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasPermission = it }
    
    LaunchedEffect(Unit) { launcher.launch(Manifest.permission.CAMERA) }

    Column(modifier = Modifier.fillMaxSize()) {
        if (hasPermission) {
            Box(modifier = Modifier.weight(1f).padding(16.dp).clip(RoundedCornerShape(24.dp))) {
                CameraPreview(imageCapture = imageCapture)
                // Overlay for capture
                Box(modifier = Modifier.fillMaxSize().padding(bottom = 20.dp), contentAlignment = Alignment.BottomCenter) {
                    FloatingActionButton(onClick = { onCapture(imageCapture) }, containerColor = Blanco, contentColor = AzulOscuro) {
                        Icon(Icons.Default.Camera, contentDescription = "Capturar")
                    }
                }
            }
        } else {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(text = "Se requieren permisos de cámara")
            }
        }
        
        LazyRow(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(images) { uri ->
                Box(modifier = Modifier.size(100.dp).clip(RoundedCornerShape(12.dp))) {
                    AsyncImage(model = uri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    IconButton(onClick = { onRemove(uri) }, modifier = Modifier.align(Alignment.TopEnd).background(Rojo.copy(alpha = 0.5f), CircleShape)) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Blanco, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryStep(
    machine: com.biobox.biotech.domain.model.Machine?,
    items: Map<Int, Int>,
    observaciones: String,
    onObsChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text = "Finalizar Revisión", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        
        OutlinedTextField(
            value = observaciones,
            onValueChange = onObsChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Observaciones") },
            placeholder = { Text("Describe el estado general...") },
            minLines = 4
        )
        
        Text(text = "Resumen de cambios:", fontWeight = FontWeight.Bold)
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = GrisCard)) {
            Column(modifier = Modifier.padding(16.dp)) {
                items.forEach { (id, qty) ->
                    // Logic to find material name could be here or in VM
                }
                Text("Cálculo de integración será procesado al enviar.")
            }
        }
    }
}

@Composable
fun BottomActionBar(currentStep: Int, isLoading: Boolean, onNext: () -> Unit) {
    Surface(tonalElevation = 8.dp, shadowElevation = 8.dp) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (currentStep < 3) AzulOscuro else VerdePrincipal),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Blanco, modifier = Modifier.size(24.dp))
                } else {
                    Text(text = if (currentStep < 3) "Continuar" else "Enviar Revisión", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(if (currentStep < 3) Icons.Default.ArrowForward else Icons.Default.CloudUpload, contentDescription = null)
                }
            }
        }
    }
}
