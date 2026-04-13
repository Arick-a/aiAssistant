package com.arick.aiassistant.core.network.model

data class HealthResponseDto(
    val status: String,
)

data class SummarizeRequestDto(
    val documentId: String,
    val title: String,
    val text: String,
    val mode: String = "summary",
)

data class SummarizeResponseDto(
    val documentId: String,
    val summary: String,
    val keyPoints: List<String>,
)

data class AskChunkDto(
    val chunkId: String,
    val page: Int? = null,
    val text: String,
)

data class AskRequestDto(
    val documentId: String,
    val question: String,
    val chunks: List<AskChunkDto>,
)

data class AskSourceDto(
    val chunkId: String,
    val page: Int? = null,
    val quote: String,
)

data class AskResponseDto(
    val answer: String,
    val sources: List<AskSourceDto>,
)
