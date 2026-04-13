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
)

enum class DocumentType {
    TEXT,
    PDF,
    IMAGE,
    OTHER,
}

data class SearchResult(
    val documentId: String,
    val documentTitle: String,
    val chunkIndex: Int,
    val snippet: String,
    val matchCount: Int,
)
