package com.arick.aiassistant.core.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_3 = object : Migration(1, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        createConversationTables(db)
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        createConversationTables(db)
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        addDocumentProcessingStatus(db)
    }
}

val MIGRATION_1_4 = object : Migration(1, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        createConversationTables(db)
        addDocumentProcessingStatus(db)
    }
}

val MIGRATION_2_4 = object : Migration(2, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        createConversationTables(db)
        addDocumentProcessingStatus(db)
    }
}

private fun createConversationTables(db: SupportSQLiteDatabase) {
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `conversations` (
            `id` TEXT NOT NULL,
            `documentId` TEXT NOT NULL,
            `title` TEXT NOT NULL,
            `createdAtEpochMillis` INTEGER NOT NULL,
            `updatedAtEpochMillis` INTEGER NOT NULL,
            PRIMARY KEY(`id`),
            FOREIGN KEY(`documentId`) REFERENCES `documents`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
        )
        """.trimIndent(),
    )
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_conversations_documentId` ON `conversations` (`documentId`)")
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `messages` (
            `id` TEXT NOT NULL,
            `conversationId` TEXT NOT NULL,
            `documentId` TEXT NOT NULL,
            `role` TEXT NOT NULL,
            `content` TEXT NOT NULL,
            `createdAtEpochMillis` INTEGER NOT NULL,
            PRIMARY KEY(`id`),
            FOREIGN KEY(`conversationId`) REFERENCES `conversations`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
            FOREIGN KEY(`documentId`) REFERENCES `documents`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
        )
        """.trimIndent(),
    )
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_messages_conversationId` ON `messages` (`conversationId`)")
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_messages_documentId` ON `messages` (`documentId`)")
    db.execSQL(
        """
        CREATE TABLE IF NOT EXISTS `message_sources` (
            `id` TEXT NOT NULL,
            `messageId` TEXT NOT NULL,
            `chunkId` TEXT NOT NULL,
            `page` INTEGER,
            `quote` TEXT NOT NULL,
            PRIMARY KEY(`id`),
            FOREIGN KEY(`messageId`) REFERENCES `messages`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
        )
        """.trimIndent(),
    )
    db.execSQL("CREATE INDEX IF NOT EXISTS `index_message_sources_messageId` ON `message_sources` (`messageId`)")
}

private fun addDocumentProcessingStatus(db: SupportSQLiteDatabase) {
    db.execSQL("ALTER TABLE `documents` ADD COLUMN `processingStatus` TEXT NOT NULL DEFAULT 'READY'")
}
