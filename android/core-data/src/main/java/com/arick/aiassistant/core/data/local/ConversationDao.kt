package com.arick.aiassistant.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query(
        """
        SELECT conversations.id, conversations.documentId, conversations.title,
            conversations.createdAtEpochMillis, conversations.updatedAtEpochMillis,
            documents.title AS documentTitle,
            (
                SELECT messages.content
                FROM messages
                WHERE messages.conversationId = conversations.id
                ORDER BY messages.createdAtEpochMillis DESC
                LIMIT 1
            ) AS latestMessageContent
        FROM conversations
        INNER JOIN documents ON documents.id = conversations.documentId
        ORDER BY conversations.updatedAtEpochMillis DESC
        """,
    )
    fun observeAllConversations(): Flow<List<ConversationWithDocument>>

    @Query("SELECT * FROM conversations WHERE documentId = :documentId ORDER BY updatedAtEpochMillis DESC")
    fun observeConversations(documentId: String): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :conversationId LIMIT 1")
    suspend fun getConversation(conversationId: String): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE documentId = :documentId ORDER BY updatedAtEpochMillis DESC LIMIT 1")
    suspend fun getLatestConversation(documentId: String): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Query("UPDATE conversations SET title = :title, updatedAtEpochMillis = :updatedAtEpochMillis WHERE id = :conversationId")
    suspend fun renameConversation(
        conversationId: String,
        title: String,
        updatedAtEpochMillis: Long,
    )

    @Query("UPDATE conversations SET updatedAtEpochMillis = :updatedAtEpochMillis WHERE id = :conversationId")
    suspend fun touchConversation(
        conversationId: String,
        updatedAtEpochMillis: Long,
    )

    @Transaction
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAtEpochMillis ASC")
    fun observeMessages(conversationId: String): Flow<List<MessageWithSources>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSources(sources: List<MessageSourceEntity>)

    @Transaction
    suspend fun insertMessageGraph(graph: MessageEntityGraph) {
        insertMessage(graph.message)
        if (graph.sources.isNotEmpty()) {
            insertSources(graph.sources)
        }
    }
}
