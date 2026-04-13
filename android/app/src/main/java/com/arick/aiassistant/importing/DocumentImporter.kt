package com.arick.aiassistant.importing

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.arick.aiassistant.core.ml.DocumentTextExtractor
import com.arick.aiassistant.core.ml.ExtractionResult
import com.arick.aiassistant.core.model.DocumentType
import com.arick.aiassistant.core.model.ImportedDocument
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class DocumentImporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val textExtractor: DocumentTextExtractor,
) {
    suspend fun import(uri: Uri): ImportedDocument = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        val displayName = resolver.queryDisplayName(uri) ?: "untitled"
        val mimeType = resolver.getType(uri)
        val type = detectType(displayName = displayName, mimeType = mimeType)
        val extractionResult = when (type) {
            DocumentType.TEXT -> ExtractionResult(
                text = resolver.readText(uri),
                note = null,
            )
            DocumentType.PDF -> textExtractor.extractFromPdf(uri)
            DocumentType.IMAGE -> textExtractor.extractFromImage(uri)
            DocumentType.OTHER -> ExtractionResult(
                text = "",
                note = "该文件类型已导入，但当前仅支持文本、图片和 PDF 提取。",
            )
        }

        ImportedDocument(
            id = UUID.randomUUID().toString(),
            title = displayName,
            type = type,
            uri = uri.toString(),
            mimeType = mimeType,
            extractedText = extractionResult.text,
            importNote = extractionResult.note,
            createdAt = Instant.now(),
        )
    }

    private fun detectType(displayName: String, mimeType: String?): DocumentType {
        val lowerName = displayName.lowercase()
        return when {
            mimeType?.startsWith("text/") == true -> DocumentType.TEXT
            mimeType == "application/pdf" || lowerName.endsWith(".pdf") -> DocumentType.PDF
            mimeType?.startsWith("image/") == true -> DocumentType.IMAGE
            lowerName.endsWith(".txt") || lowerName.endsWith(".md") -> DocumentType.TEXT
            else -> DocumentType.OTHER
        }
    }
}

private fun ContentResolver.queryDisplayName(uri: Uri): String? {
    return query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && nameIndex >= 0) cursor.getString(nameIndex) else null
    }
}

private fun ContentResolver.readText(uri: Uri): String {
    return openInputStream(uri)?.bufferedReader()?.use { reader ->
        reader.readText()
    }.orEmpty()
}
