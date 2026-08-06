package com.biobox.biotech.presentation.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.biobox.biotech.core.common.UiState
import com.biobox.biotech.domain.model.Document
import com.biobox.biotech.domain.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DocumentViewModel @Inject constructor(
    private val documentRepository: DocumentRepository
) : ViewModel() {

    private val _documents = MutableStateFlow<UiState<List<Document>>>(UiState.Loading)
    val documents: StateFlow<UiState<List<Document>>> = _documents.asStateFlow()

    init { loadDocuments() }

    fun loadDocuments() {
        viewModelScope.launch {
            documentRepository.getDocuments().collect { list ->
                _documents.value = UiState.Success(list)
            }
        }
        viewModelScope.launch { documentRepository.refreshDocuments() }
    }

    fun uploadDocument(document: Document, fileBytes: ByteArray, onSuccess: () -> Unit) {
        viewModelScope.launch {
            documentRepository.uploadDocument(document, fileBytes)
                .onSuccess { loadDocuments(); onSuccess() }
                .onFailure { _documents.value = UiState.Error(it.message ?: "Error") }
        }
    }

    fun deleteDocument(id: Int) {
        viewModelScope.launch {
            documentRepository.deleteDocument(id)
                .onSuccess { loadDocuments() }
                .onFailure { _documents.value = UiState.Error(it.message ?: "Error") }
        }
    }
}
