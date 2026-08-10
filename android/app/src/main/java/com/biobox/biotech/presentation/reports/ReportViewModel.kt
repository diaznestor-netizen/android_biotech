package com.biobox.biotech.presentation.reports

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biobox.biotech.core.common.UiState
import com.biobox.biotech.data.remote.api.ReportService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

data class ReportState(
    val isLoading: Boolean = false,
    val downloadedFile: File? = null,
    val error: String? = null
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val reportService: ReportService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow(ReportState())
    val state: StateFlow<ReportState> = _state.asStateFlow()

    fun downloadGlobalExcel() {
        viewModelScope.launch {
            _state.value = ReportState(isLoading = true)
            try {
                val response = withContext(Dispatchers.IO) {
					reportService.getGlobalExcel()
                }
                if (response.isSuccessful) {
                    val body = response.body() ?: throw Exception("Respuesta vacía")
                    val fileName = "Biotech_Reporte_Global_${System.currentTimeMillis()}.xlsx"
                    val file = File(context.getExternalFilesDir(null), fileName)
                    withContext(Dispatchers.IO) {
                        FileOutputStream(file).use { output ->
                            body.byteStream().use { input -> input.copyTo(output) }
                        }
                    }
                    _state.value = ReportState(downloadedFile = file)
                } else {
                    _state.value = ReportState(error = "Error ${response.code()} al descargar")
                }
            } catch (e: Exception) {
                _state.value = ReportState(error = e.message ?: "Error de descarga")
            }
        }
    }

    fun clearState() { _state.value = ReportState() }
}
