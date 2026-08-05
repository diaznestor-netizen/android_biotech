package com.biobox.biotech.data.repository

import com.biobox.biotech.data.local.dao.DocumentDao
import com.biobox.biotech.data.mapper.toDomain
import com.biobox.biotech.data.mapper.toEntity
import com.biobox.biotech.data.remote.api.DocumentService
import com.biobox.biotech.domain.model.Document
import com.biobox.biotech.domain.repository.DocumentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class DocumentRepositoryImpl @Inject constructor(
    private val documentService: DocumentService,
    private val documentDao: DocumentDao
) : DocumentRepository {

    override fun getDocuments(): Flow<List<Document>> {
        return documentDao.getAllDocuments().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getDocumentsByMachine(machineId: Int): Flow<List<Document>> {
        return documentDao.getDocumentsByMachine(machineId).map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun refreshDocuments() {
        try {
            val response = documentService.getDocuments()
            if (response.isSuccessful) {
                val documents = response.body().orEmpty().map { it.toEntity() }
                documentDao.insertDocuments(documents)
            }
        } catch (_: Exception) { }
    }

    override suspend fun uploadDocument(document: Document, fileBytes: ByteArray): Result<Document> = runCatching {
        val tituloBody = document.titulo.toRequestBody("text/plain".toMediaTypeOrNull())
        val tipoBody = document.tipo.name.toRequestBody("text/plain".toMediaTypeOrNull())
        val maquinaIdBody = document.maquinaId?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
        val fileBody = fileBytes.toRequestBody("application/octet-stream".toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("file", "documento", fileBody)

        val response = documentService.uploadDocument(tituloBody, tipoBody, maquinaIdBody, filePart)
        if (response.isSuccessful) {
            val dto = response.body() ?: throw Exception("Respuesta vacía")
            val entity = dto.toEntity()
            documentDao.insertDocuments(listOf(entity))
            entity.toDomain()
        } else throw Exception("Error al subir documento: ${response.code()}")
    }

    override suspend fun deleteDocument(id: Int): Result<Unit> = runCatching {
        val response = documentService.deleteDocument(id)
        if (!response.isSuccessful) throw Exception("Error al eliminar: ${response.code()}")
        documentDao.deleteDocument(id)
    }
}
