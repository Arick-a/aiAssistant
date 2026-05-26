package com.arick.aiassistant.core.data.local

import com.arick.aiassistant.core.model.DocumentProcessingStatus
import com.arick.aiassistant.core.model.DocumentType
import com.arick.aiassistant.core.model.ImportedDocument
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class DocumentMappersTest {
    @Test
    fun `document processing status maps to entity and back`() {
        val document = ImportedDocument(
            id = "doc-1",
            title = "contract.pdf",
            type = DocumentType.PDF,
            uri = "content://contract",
            mimeType = "application/pdf",
            extractedText = "合同内容",
            importNote = null,
            createdAt = Instant.ofEpochMilli(1_700_000_000_000),
            processingStatus = DocumentProcessingStatus.READY,
        )

        val restored = document.asEntity().asExternalModel()

        assertEquals("READY", document.asEntity().processingStatus)
        assertEquals(document, restored)
    }
}
