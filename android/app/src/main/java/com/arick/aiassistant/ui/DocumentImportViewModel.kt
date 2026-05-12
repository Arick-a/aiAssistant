package com.arick.aiassistant.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arick.aiassistant.core.data.DocumentRepository
import com.arick.aiassistant.core.network.AiAssistantApiService
import com.arick.aiassistant.core.network.model.AskChunkDto
import com.arick.aiassistant.core.network.model.AskRequestDto
import com.arick.aiassistant.core.network.model.SummarizeRequestDto
import com.arick.aiassistant.core.model.ImportedDocument
import com.arick.aiassistant.core.model.SearchResult
import com.arick.aiassistant.core.ml.KeywordSearchEngine
import com.arick.aiassistant.importing.DocumentImporter
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class DocumentImportViewModel @Inject constructor(
    private val repository: DocumentRepository,
    private val importer: DocumentImporter,
    private val searchEngine: KeywordSearchEngine,
    private val apiService: AiAssistantApiService,
) : ViewModel() {
    private val importStatus = MutableStateFlow(false)
    private val statusMessage = MutableStateFlow<String?>(null)
    private val selectedDocument = MutableStateFlow<ImportedDocument?>(null)
    private val searchQuery = MutableStateFlow("")
    private val aiInteractionState = MutableStateFlow(AiInteractionState())
    val selectedDocumentState: StateFlow<ImportedDocument?> = selectedDocument

    val uiState: StateFlow<DocumentImportUiState> = combine(
        repository.observeDocuments(),
        importStatus,
        statusMessage,
        searchQuery,
        aiInteractionState,
    ) { documents, isImporting, status, query, aiState ->
            DocumentImportUiState(
                documents = documents,
                isImporting = isImporting,
                statusMessage = status,
                searchQuery = query,
                searchResults = searchEngine.search(documents, query),
                question = aiState.question,
                summary = aiState.summary,
                answer = aiState.answer,
                sources = aiState.sources,
                isSummarizing = aiState.isSummarizing,
                isAsking = aiState.isAsking,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DocumentImportUiState(),
        )

    fun importDocument(uri: Uri) {
        viewModelScope.launch {
            importStatus.value = true
            statusMessage.value = null
            runCatching {
                val document = importer.import(uri)
                repository.addDocument(document)
                statusMessage.value = "已导入 ${document.title}"
            }.onFailure { throwable ->
                statusMessage.value = throwable.message ?: "导入失败"
            }
            importStatus.value = false
        }
    }

    fun loadDocument(documentId: String) {
        viewModelScope.launch {
            val previousDocumentId = selectedDocument.value?.id
            val document = repository.getDocument(documentId)
            selectedDocument.value = document
            if (previousDocumentId != document?.id) {
                aiInteractionState.value = AiInteractionState()
            }
        }
    }

    fun clearStatusMessage() {
        statusMessage.value = null
    }

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun summarizeSelectedDocument() {
        val document = selectedDocument.value ?: return
        if (document.extractedText.isBlank()) {
            statusMessage.value = "当前文档没有可总结的文本"
            return
        }

        viewModelScope.launch {
            aiInteractionState.value = aiInteractionState.value.copy(
                isSummarizing = true,
                summary = "",
            )
            runCatching {
                apiService.summarize(
                    SummarizeRequestDto(
                        documentId = document.id,
                        title = document.title,
                        text = document.extractedText,
                    ),
                )
            }.onSuccess { response ->
                aiInteractionState.value = aiInteractionState.value.copy(
                    isSummarizing = false,
                    summary = buildSummaryText(response.summary, response.keyPoints),
                )
            }.onFailure { throwable ->
                aiInteractionState.value = aiInteractionState.value.copy(isSummarizing = false)
                statusMessage.value = throwable.message ?: "生成摘要失败"
            }
        }
    }

    fun updateQuestion(question: String) {
        aiInteractionState.value = aiInteractionState.value.copy(question = question)
    }

    fun askSelectedDocument() {
        val document = selectedDocument.value ?: return
        val question = aiInteractionState.value.question.trim()
        if (question.isBlank()) {
            statusMessage.value = "请先输入问题"
            return
        }
        if (document.extractedText.isBlank()) {
            statusMessage.value = "当前文档没有可问答的文本"
            return
        }

        viewModelScope.launch {
            aiInteractionState.value = aiInteractionState.value.copy(
                isAsking = true,
                answer = "",
                sources = emptyList(),
            )
            runCatching {
                apiService.ask(
                    AskRequestDto(
                        documentId = document.id,
                        question = question,
                        chunks = document.toAskChunks(),
                    ),
                )
            }.onSuccess { response ->
                aiInteractionState.value = aiInteractionState.value.copy(
                    isAsking = false,
                    answer = response.answer,
                    sources = response.sources.map { it.quote },
                )
            }.onFailure { throwable ->
                aiInteractionState.value = aiInteractionState.value.copy(isAsking = false)
                statusMessage.value = throwable.message ?: "提问失败"
            }
        }
    }
}

data class DocumentImportUiState(
    val documents: List<ImportedDocument> = emptyList(),
    val isImporting: Boolean = false,
    val statusMessage: String? = null,
    val searchQuery: String = "",
    val searchResults: List<SearchResult> = emptyList(),
    val question: String = "",
    val summary: String = "",
    val answer: String = "",
    val sources: List<String> = emptyList(),
    val isSummarizing: Boolean = false,
    val isAsking: Boolean = false,
)

private data class AiInteractionState(
    val question: String = "",
    val summary: String = "",
    val answer: String = "",
    val sources: List<String> = emptyList(),
    val isSummarizing: Boolean = false,
    val isAsking: Boolean = false,
)

private fun ImportedDocument.toAskChunks(
    chunkSize: Int = 800,
    chunkOverlap: Int = 120,
): List<AskChunkDto> {
    val normalizedText = extractedText.trim()
    if (normalizedText.isBlank()) return emptyList()

    val chunks = mutableListOf<AskChunkDto>()
    var start = 0
    var index = 0
    while (start < normalizedText.length) {
        val end = minOf(start + chunkSize, normalizedText.length)
        chunks += AskChunkDto(
            chunkId = "$id-$index",
            text = normalizedText.substring(start, end),
        )
        if (end == normalizedText.length) break
        start = maxOf(0, end - chunkOverlap)
        index += 1
    }
    return chunks
}

private fun buildSummaryText(
    summary: String,
    keyPoints: List<String>,
): String {
    if (keyPoints.isEmpty()) return summary
    return buildString {
        append(summary)
        append("\n\n要点：")
        keyPoints.forEach { keyPoint ->
            append("\n- ")
            append(keyPoint)
        }
    }
}
