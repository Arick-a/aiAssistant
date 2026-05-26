package com.arick.aiassistant.core.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class MessageWithSources(
    @Embedded val message: MessageEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "messageId",
    )
    val sources: List<MessageSourceEntity>,
)
