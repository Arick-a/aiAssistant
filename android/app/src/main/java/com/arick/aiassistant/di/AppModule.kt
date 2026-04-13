package com.arick.aiassistant.di

import com.arick.aiassistant.core.data.DocumentRepository
import com.arick.aiassistant.core.data.RoomDocumentRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    @Singleton
    abstract fun bindDocumentRepository(
        repository: RoomDocumentRepository,
    ): DocumentRepository
}
