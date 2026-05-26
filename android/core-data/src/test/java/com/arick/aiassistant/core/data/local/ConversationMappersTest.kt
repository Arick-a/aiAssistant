package com.arick.aiassistant.core.data.local

import com.arick.aiassistant.core.model.ConversationMessage
import com.arick.aiassistant.core.model.DocumentConversation
import com.arick.aiassistant.core.model.MessageRole
import com.arick.aiassistant.core.model.MessageSource
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class ConversationMappersTest {
    @Test
    fun `message with sources maps to entity graph and back`() {
        val message = ConversationMessage(
            id = "message-1",
            conversationId = "conversation-1",
            documentId = "doc-1",
            role = MessageRole.ASSISTANT,
            content = "回答内容",
            createdAt = Instant.ofEpochMilli(1_700_000_000_000),
            sources = listOf(
                MessageSource(
                    id = "source-1",
                    messageId = "message-1",
                    chunkId = "doc-1-0",
                    page = 3,
                    quote = "引用片段",
                ),
            ),
        )

        val graph = message.asEntityGraph()
        val restored = graph.asExternalModel()

        assertEquals("ASSISTANT", graph.message.role)
        assertEquals("conversation-1", graph.message.conversationId)
        assertEquals(1_700_000_000_000, graph.message.createdAtEpochMillis)
        assertEquals("source-1", graph.sources.single().id)
        assertEquals(message, restored)
    }

    @Test
    fun `conversation maps to entity and back`() {
        val conversation = DocumentConversation(
            id = "conversation-1",
            documentId = "doc-1",
            title = "默认会话",
            createdAt = Instant.ofEpochMilli(1_700_000_000_000),
            updatedAt = Instant.ofEpochMilli(1_700_000_000_500),
        )

        val restored = conversation.asEntity().asExternalModel()

        assertEquals(conversation, restored)
    }

    @Test
    fun `conversation with document title maps to list item`() {
        val conversation = ConversationWithDocument(
            id = "conversation-1",
            documentId = "doc-1",
            title = "合同问答",
            createdAtEpochMillis = 1_700_000_000_000,
            updatedAtEpochMillis = 1_700_000_000_500,
            documentTitle = "合同.pdf",
            latestMessageContent = "这份合同的付款周期是什么？",
        )

        val item = conversation.asExternalModel()

        assertEquals("conversation-1", item.id)
        assertEquals("doc-1", item.documentId)
        assertEquals("合同问答", item.title)
        assertEquals("合同.pdf", item.documentTitle)
        assertEquals("这份合同的付款周期是什么？", item.latestMessagePreview)
        assertEquals(Instant.ofEpochMilli(1_700_000_000_500), item.updatedAt)
    }
}
