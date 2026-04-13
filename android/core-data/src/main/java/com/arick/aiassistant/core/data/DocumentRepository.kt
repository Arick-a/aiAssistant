package com.arick.aiassistant.core.data

import com.arick.aiassistant.core.model.ImportedDocument
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    fun observeDocuments(): Flow<List<ImportedDocument>>

    suspend fun addDocument(document: ImportedDocument)

    suspend fun getDocument(documentId: String): ImportedDocument?
}
