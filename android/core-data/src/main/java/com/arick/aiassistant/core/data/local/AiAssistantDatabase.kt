package com.arick.aiassistant.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [DocumentEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AiAssistantDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao
}
