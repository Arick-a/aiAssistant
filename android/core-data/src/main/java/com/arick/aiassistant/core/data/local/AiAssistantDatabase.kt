package com.arick.aiassistant.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        DocumentEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        MessageSourceEntity::class,
    ],
    version = 4,
    exportSchema = false,
)
abstract class AiAssistantDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao
    abstract fun conversationDao(): ConversationDao
}
