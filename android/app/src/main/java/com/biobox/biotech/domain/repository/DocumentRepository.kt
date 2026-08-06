package com.biobox.biotech.domain.repository

import com.biobox.biotech.domain.model.Document
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    fun getDocuments(): Flow<List<Document>>
    fun getDocumentsByMachine(machineId: Int): Flow<List<Document>>
    suspend fun refreshDocuments()
    suspend fun uploadDocument(document: Document, fileBytes: ByteArray): Result<Document>
    suspend fun deleteDocument(id: Int): Result<Unit>
}
