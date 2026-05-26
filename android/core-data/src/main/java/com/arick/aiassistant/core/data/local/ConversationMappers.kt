package com.arick.aiassistant.core.data.local

import com.arick.aiassistant.core.model.ConversationMessage
import com.arick.aiassistant.core.model.ConversationListItem
import com.arick.aiassistant.core.model.DocumentConversation
import com.arick.aiassistant.core.model.MessageRole
import com.arick.aiassistant.core.model.MessageSource
import java.time.Instant

data class MessageEntityGraph(
    val message: MessageEntity,
    val sources: List<MessageSourceEntity>,
)

fun ConversationMessage.asEntityGraph(): MessageEntityGraph {
    return MessageEntityGraph(
        message = MessageEntity(
            id = id,
            conversationId = conversationId,
            documentId = documentId,
            role = role.name,
            content = content,
            createdAtEpochMillis = createdAt.toEpochMilli(),
        ),
        sources = sources.map { source ->
            MessageSourceEntity(
                id = source.id,
                messageId = id,
                chunkId = source.chunkId,
                page = source.page,
                quote = source.quote,
            )
        },
    )
}

fun MessageEntityGraph.asExternalModel(): ConversationMessage {
    return ConversationMessage(
        id = message.id,
        conversationId = message.conversationId,
        documentId = message.documentId,
        role = MessageRole.valueOf(message.role),
        content = message.content,
        createdAt = Instant.ofEpochMilli(message.createdAtEpochMillis),
        sources = sources.map { source ->
            MessageSource(
                id = source.id,
                messageId = source.messageId,
                chunkId = source.chunkId,
                page = source.page,
                quote = source.quote,
            )
        },
    )
}

fun MessageWithSources.asExternalModel(): ConversationMessage {
    return MessageEntityGraph(
        message = message,
        sources = sources,
    ).asExternalModel()
}

fun DocumentConversation.asEntity(): ConversationEntity {
    return ConversationEntity(
        id = id,
        documentId = documentId,
        title = title,
        createdAtEpochMillis = createdAt.toEpochMilli(),
        updatedAtEpochMillis = updatedAt.toEpochMilli(),
    )
}

fun ConversationEntity.asExternalModel(): DocumentConversation {
    return DocumentConversation(
        id = id,
        documentId = documentId,
        title = title,
        createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
        updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
    )
}

fun ConversationWithDocument.asExternalModel(): ConversationListItem {
    return ConversationListItem(
        id = id,
        documentId = documentId,
        title = title,
        documentTitle = documentTitle,
        latestMessagePreview = latestMessageContent?.trim()?.takeIf { it.isNotBlank() },
        createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
        updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
    )
}
