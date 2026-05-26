package com.arick.aiassistant.ui

import com.arick.aiassistant.core.model.DocumentConversation
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class ConversationTitleTest {
    @Test
    fun `nextConversationTitle skips existing numbered titles`() {
        val conversations = listOf(
            conversation(title = "默认会话"),
            conversation(title = "新会话 1"),
            conversation(title = "新会话 2"),
        )

        val title = nextConversationTitle(conversations)

        assertEquals("新会话 3", title)
    }

    @Test
    fun `nextConversationTitle fills the first missing number`() {
        val conversations = listOf(
            conversation(title = "新会话 1"),
            conversation(title = "新会话 3"),
        )

        val title = nextConversationTitle(conversations)

        assertEquals("新会话 2", title)
    }

    private fun conversation(title: String): DocumentConversation {
        return DocumentConversation(
            id = title,
            documentId = "doc-1",
            title = title,
            createdAt = Instant.ofEpochMilli(1_700_000_000_000),
            updatedAt = Instant.ofEpochMilli(1_700_000_000_000),
        )
    }
}
