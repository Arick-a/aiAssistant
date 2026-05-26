package com.arick.aiassistant.core.model

import java.time.Instant

data class ImportedDocument(
    val id: String,
    val title: String,
    val type: DocumentType,
    val uri: String,
    val mimeType: String?,
    val extractedText: String,
    val importNote: String?,
    val createdAt: Instant,
    val processingStatus: DocumentProcessingStatus = DocumentProcessingStatus.READY,
)

enum class DocumentType {
    TEXT,
    PDF,
    IMAGE,
    OTHER,
}

enum class DocumentProcessingStatus {
    IMPORTED,
    PARSING,
    READY,
    FAILED,
}

data class SearchResult(
    val documentId: String,
    val documentTitle: String,
    val chunkIndex: Int,
    val snippet: String,
    val matchCount: Int,
)

data class ConversationMessage(
    val id: String,
    val conversationId: String,
    val documentId: String,
    val role: MessageRole,
    val content: String,
    val createdAt: Instant,
    val sources: List<MessageSource> = emptyList(),
)

enum class MessageRole {
    USER,
    ASSISTANT,
}

data class MessageSource(
    val id: String,
    val messageId: String,
    val chunkId: String,
    val page: Int?,
    val quote: String,
)

data class DocumentConversation(
    val id: String,
    val documentId: String,
    val title: String,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class ConversationListItem(
    val id: String,
    val documentId: String,
    val title: String,
    val documentTitle: String,
    val latestMessagePreview: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
)
