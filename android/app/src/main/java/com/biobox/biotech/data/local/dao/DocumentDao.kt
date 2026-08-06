package com.biobox.biotech.data.local.dao

import androidx.room.*
import com.biobox.biotech.data.local.entity.DocumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY fechaSubida DESC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE maquinaId = :machineId ORDER BY fechaSubida DESC")
    fun getDocumentsByMachine(machineId: Int): Flow<List<DocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocuments(documents: List<DocumentEntity>)

    @Query("DELETE FROM documents")
    suspend fun deleteAll()

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocument(id: Int)
}
