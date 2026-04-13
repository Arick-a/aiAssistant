package com.arick.aiassistant.core.data.local

import com.arick.aiassistant.core.model.DocumentType
import com.arick.aiassistant.core.model.ImportedDocument
import java.time.Instant

fun DocumentEntity.asExternalModel(): ImportedDocument {
    return ImportedDocument(
        id = id,
        title = title,
        type = DocumentType.valueOf(type),
        uri = uri,
        mimeType = mimeType,
        extractedText = extractedText,
        importNote = importNote,
        createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
    )
}

fun ImportedDocument.asEntity(): DocumentEntity {
    return DocumentEntity(
        id = id,
        title = title,
        type = type.name,
        uri = uri,
        mimeType = mimeType,
        extractedText = extractedText,
        importNote = importNote,
        createdAtEpochMillis = createdAt.toEpochMilli(),
    )
}
