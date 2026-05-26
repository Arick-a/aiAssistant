package com.arick.aiassistant.core.data

import com.arick.aiassistant.core.data.local.ConversationDao
import com.arick.aiassistant.core.data.local.DocumentDao
import com.arick.aiassistant.core.data.local.asEntity
import com.arick.aiassistant.core.data.local.asEntityGraph
import com.arick.aiassistant.core.data.local.asExternalModel
import com.arick.aiassistant.core.model.ConversationMessage
import com.arick.aiassistant.core.model.ConversationListItem
import com.arick.aiassistant.core.model.DocumentConversation
import com.arick.aiassistant.core.model.ImportedDocument
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RoomDocumentRepository @Inject constructor(
    private val documentDao: DocumentDao,
    private val conversationDao: ConversationDao,
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

    override fun observeAllConversations(): Flow<List<ConversationListItem>> {
        return conversationDao.observeAllConversations().map { conversations ->
            conversations.map { it.asExternalModel() }
        }
    }

    override fun observeConversations(documentId: String): Flow<List<DocumentConversation>> {
        return conversationDao.observeConversations(documentId).map { conversations ->
            conversations.map { it.asExternalModel() }
        }
    }

    override fun observeMessages(conversationId: String): Flow<List<ConversationMessage>> {
        return conversationDao.observeMessages(conversationId).map { messages ->
            messages.map { it.asExternalModel() }
        }
    }

    override suspend fun getConversation(conversationId: String): DocumentConversation? {
        return conversationDao.getConversation(conversationId)?.asExternalModel()
    }

    override suspend fun getOrCreateConversation(
        documentId: String,
        defaultTitle: String,
    ): DocumentConversation {
        val existing = conversationDao.getLatestConversation(documentId)
        if (existing != null) return existing.asExternalModel()
        return createConversation(documentId, defaultTitle)
    }

    override suspend fun createConversation(
        documentId: String,
        title: String,
    ): DocumentConversation {
        val now = Instant.now()
        val conversation = DocumentConversation(
            id = UUID.randomUUID().toString(),
            documentId = documentId,
            title = title,
            createdAt = now,
            updatedAt = now,
        )
        conversationDao.insertConversation(conversation.asEntity())
        return conversation
    }

    override suspend fun renameConversation(
        conversationId: String,
        title: String,
    ) {
        conversationDao.renameConversation(
            conversationId = conversationId,
            title = title,
            updatedAtEpochMillis = Instant.now().toEpochMilli(),
        )
    }

    override suspend fun addMessage(message: ConversationMessage) {
        conversationDao.insertMessageGraph(message.asEntityGraph())
        conversationDao.touchConversation(
            conversationId = message.conversationId,
            updatedAtEpochMillis = message.createdAt.toEpochMilli(),
        )
    }
}
