package com.arick.aiassistant.core.network

import com.arick.aiassistant.core.network.model.AskRequestDto
import com.arick.aiassistant.core.network.model.AskResponseDto
import com.arick.aiassistant.core.network.model.HealthResponseDto
import com.arick.aiassistant.core.network.model.SummarizeRequestDto
import com.arick.aiassistant.core.network.model.SummarizeResponseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AiAssistantApiService {
    @GET("health")
    suspend fun health(): HealthResponseDto

    @POST("summarize")
    suspend fun summarize(
        @Body request: SummarizeRequestDto,
    ): SummarizeResponseDto

    @POST("ask")
    suspend fun ask(
        @Body request: AskRequestDto,
    ): AskResponseDto
}
