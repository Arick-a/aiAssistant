package com.arick.aiassistant.core.data.local

data class ConversationWithDocument(
    val id: String,
    val documentId: String,
    val title: String,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
    val documentTitle: String,
    val latestMessageContent: String?,
)
