package com.arick.aiassistant.core.data

import com.arick.aiassistant.core.model.ImportedDocument
import com.arick.aiassistant.core.model.ConversationMessage
import com.arick.aiassistant.core.model.ConversationListItem
import com.arick.aiassistant.core.model.DocumentConversation
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    fun observeDocuments(): Flow<List<ImportedDocument>>

    suspend fun addDocument(document: ImportedDocument)

    suspend fun getDocument(documentId: String): ImportedDocument?

    fun observeAllConversations(): Flow<List<ConversationListItem>>

    fun observeConversations(documentId: String): Flow<List<DocumentConversation>>

    fun observeMessages(conversationId: String): Flow<List<ConversationMessage>>

    suspend fun getConversation(conversationId: String): DocumentConversation?

    suspend fun getOrCreateConversation(
        documentId: String,
        defaultTitle: String,
    ): DocumentConversation

    suspend fun createConversation(
        documentId: String,
        title: String,
    ): DocumentConversation

    suspend fun renameConversation(
        conversationId: String,
        title: String,
    )

    suspend fun addMessage(message: ConversationMessage)
}
