package com.arick.aiassistant.core.data

import com.arick.aiassistant.core.data.local.DocumentDao
import com.arick.aiassistant.core.data.local.asEntity
import com.arick.aiassistant.core.data.local.asExternalModel
import com.arick.aiassistant.core.model.ImportedDocument
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RoomDocumentRepository @Inject constructor(
    private val documentDao: DocumentDao,
) : DocumentRepository {
    override fun observeDocuments(): Flow<List<ImportedDocument>> {
        return documentDao.observeDocuments().map { entities ->
            entities.map { it.asExternalModel() }
        }
    }

    override suspend fun addDocument(document: ImportedDocument) {
        documentDao.insertDocument(document.asEntity())
    }

    override suspend fun getDocument(documentId: String): ImportedDocument? {
        return documentDao.getDocumentById(documentId)?.asExternalModel()
    }
}
