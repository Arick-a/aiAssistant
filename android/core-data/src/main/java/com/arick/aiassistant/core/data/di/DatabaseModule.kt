package com.arick.aiassistant.core.data.di

import android.content.Context
import androidx.room.Room
import com.arick.aiassistant.core.data.local.AiAssistantDatabase
import com.arick.aiassistant.core.data.local.DocumentDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAiAssistantDatabase(
        @ApplicationContext context: Context,
    ): AiAssistantDatabase {
        return Room.databaseBuilder(
            context,
            AiAssistantDatabase::class.java,
            "ai_assistant.db",
        ).build()
    }

    @Provides
    fun provideDocumentDao(database: AiAssistantDatabase): DocumentDao {
        return database.documentDao()
    }
}
