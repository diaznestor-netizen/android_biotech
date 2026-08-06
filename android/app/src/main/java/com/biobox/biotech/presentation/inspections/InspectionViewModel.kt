package com.biobox.biotech.presentation.inspections

import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biobox.biotech.domain.model.Inspection
import com.biobox.biotech.domain.model.InspectionItem
import com.biobox.biotech.domain.model.Machine
import com.biobox.biotech.domain.notifications.NotificationCenter
import com.biobox.biotech.domain.notifications.NotificationEvent
import com.biobox.biotech.domain.repository.MachineRepository
import com.biobox.biotech.domain.usecase.SubmitInspectionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class InspectionUIState(
    val currentStep: Int = 0,
    val isLoading: Boolean = false,
    val machine: Machine? = null,
    val items: Map<Int, Int> = emptyMap(),
    val observaciones: String = "",
    val capturedImages: List<Uri> = emptyList(),
    val isSubmitted: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class InspectionViewModel @Inject constructor(
    private val machineRepository: MachineRepository,
    private val submitInspectionUseCase: SubmitInspectionUseCase,
    private val notificationCenter: NotificationCenter,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val machineId: Int = checkNotNull(savedStateHandle["machineId"])
    private val _state = MutableStateFlow(InspectionUIState())
    val state: StateFlow<InspectionUIState> = _state.asStateFlow()

    init {
        loadMachine()
    }

    private fun loadMachine() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            machineRepository.getMachineById(machineId)
                .filterNotNull()
                .collect { machine ->
                    val initialItems = mutableMapOf<Int, Int>()
                    machine.componentes.flatMap { it.materiales }.forEach {
                        it.id?.let { materialId ->
                            initialItems[materialId] = it.cantidadDisponible
                        }
                    }
                    _state.update { it.copy(
                        isLoading = false,
                        machine = machine,
                        items = initialItems
                    ) }
                }
        }
    }

    fun nextStep() {
        if (_state.value.currentStep < 3) {
            _state.update { it.copy(currentStep = it.currentStep + 1) }
        }
    }

    fun prevStep() {
        if (_state.value.currentStep > 0) {
            _state.update { it.copy(currentStep = it.currentStep - 1) }
        }
    }

    fun onQuantityChange(materialId: Int, quantity: Int) {
        val currentItems = _state.value.items.toMutableMap()
        currentItems[materialId] = quantity
        _state.update { it.copy(items = currentItems) }
    }

    fun onObservacionesChange(obs: String) {
        _state.update { it.copy(observaciones = obs) }
    }

    fun captureImage(imageCapture: ImageCapture) {
        val outputDirectory = File(context.cacheDir, "inspections").apply { mkdirs() }
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    _state.update { it.copy(capturedImages = it.capturedImages + savedUri) }
                }

                override fun onError(exception: ImageCaptureException) {
                    _state.update { it.copy(error = "Error al capturar imagen") }
                }
            }
        )
    }

    fun removeImage(uri: Uri) {
        _state.update { it.copy(capturedImages = it.capturedImages.filter { it != uri }) }
    }

    fun submitInspection() {
        viewModelScope.launch {
            val currentMachine = _state.value.machine
            _state.update { it.copy(isLoading = true) }
            val inspection = Inspection(
                machineId = machineId,
                items = _state.value.items.map { InspectionItem(it.key, it.value) },
                observaciones = _state.value.observaciones,
                evidencePaths = _state.value.capturedImages.map { it.path ?: "" }
            )
            
            submitInspectionUseCase(inspection)
                .onSuccess {
                    _state.update { it.copy(isLoading = false, isSubmitted = true) }
                    
                    // Notify based on observations or machine status
                    notificationCenter.notify(
                        NotificationEvent.MachineRepairCompleted(
                            id = machineId.toString(),
                            name = currentMachine?.nombre ?: "M-$machineId",
                            technician = "Operario Android"
                        )
                    )
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }
}
