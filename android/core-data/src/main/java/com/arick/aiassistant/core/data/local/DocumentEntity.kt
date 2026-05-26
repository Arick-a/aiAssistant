package com.arick.aiassistant.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey val id: String,
    val title: String,
    val type: String,
    val uri: String,
    val mimeType: String?,
    val extractedText: String,
    val importNote: String?,
    val createdAtEpochMillis: Long,
    val processingStatus: String,
)
