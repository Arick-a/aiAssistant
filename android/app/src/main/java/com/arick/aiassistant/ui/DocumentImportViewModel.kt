package com.arick.aiassistant.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arick.aiassistant.core.data.DocumentRepository
import com.arick.aiassistant.core.network.AiAssistantApiService
import com.arick.aiassistant.core.network.NetworkConfig
import com.arick.aiassistant.core.network.model.AskChunkDto
import com.arick.aiassistant.core.network.model.AskRequestDto
import com.arick.aiassistant.core.network.model.SummarizeRequestDto
import com.arick.aiassistant.core.model.ConversationMessage
import com.arick.aiassistant.core.model.ConversationListItem
import com.arick.aiassistant.core.model.DocumentConversation
import com.arick.aiassistant.core.model.ImportedDocument
import com.arick.aiassistant.core.model.MessageRole
import com.arick.aiassistant.core.model.MessageSource
import com.arick.aiassistant.core.model.SearchResult
import com.arick.aiassistant.core.ml.KeywordSearchEngine
import com.arick.aiassistant.importing.DocumentImporter
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class DocumentImportViewModel @Inject constructor(
    private val repository: DocumentRepository,
    private val importer: DocumentImporter,
    private val searchEngine: KeywordSearchEngine,
    private val apiService: AiAssistantApiService,
) : ViewModel() {
    private val importStatus = MutableStateFlow(false)
    private val statusMessage = MutableStateFlow<String?>(null)
    private val selectedDocument = MutableStateFlow<ImportedDocument?>(null)
    private val selectedConversation = MutableStateFlow<DocumentConversation?>(null)
    private val conversationTitleInput = MutableStateFlow("")
    private val searchQuery = MutableStateFlow("")
    private val aiInteractionState = MutableStateFlow(AiInteractionState())
    private val backendHealth = MutableStateFlow(
        BackendHealthUiState(baseUrl = NetworkConfig.DEFAULT_BASE_URL),
    )
    val selectedDocumentState: StateFlow<ImportedDocument?> = selectedDocument
    private val conversations = selectedDocument.flatMapLatest { document ->
        if (document == null) {
            flowOf(emptyList())
        } else {
            repository.observeConversations(document.id)
        }
    }
    private val conversationMessages = selectedConversation.flatMapLatest { conversation ->
        if (conversation == null) {
            flowOf(emptyList())
        } else {
            repository.observeMessages(conversation.id)
        }
    }

    private val baseContentUiState = combine(
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

    private val contentUiState = combine(
        baseContentUiState,
        conversationMessages,
        repository.observeAllConversations(),
    ) { contentState, messages, allConversations ->
        contentState.copy(
            conversationMessages = messages,
            allConversations = allConversations,
        )
    }

    private val conversationUiState = combine(
        contentUiState,
        conversations,
        selectedConversation,
        conversationTitleInput,
    ) { contentState, conversationList, currentConversation, titleInput ->
        contentState.copy(
            conversations = conversationList,
            selectedConversationId = currentConversation?.id,
            conversationTitleInput = titleInput,
        )
    }

    val uiState: StateFlow<DocumentImportUiState> = combine(
        conversationUiState,
        backendHealth,
    ) { contentState, healthState ->
        contentState.copy(backendHealth = healthState)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DocumentImportUiState(),
        )

    init {
        checkBackendHealth()
    }

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

    fun loadDocument(
        documentId: String,
        conversationId: String? = null,
    ) {
        viewModelScope.launch {
            val previousDocumentId = selectedDocument.value?.id
            val document = repository.getDocument(documentId)
            selectedDocument.value = document
            if (document == null) {
                selectedConversation.value = null
                conversationTitleInput.value = ""
                aiInteractionState.value = AiInteractionState()
                return@launch
            }
            if (previousDocumentId != document.id || conversationId != null) {
                aiInteractionState.value = AiInteractionState()
                val conversation = conversationId
                    ?.let { repository.getConversation(it) }
                    ?.takeIf { it.documentId == document.id }
                    ?: repository.getOrCreateConversation(
                        documentId = document.id,
                        defaultTitle = "默认会话",
                    )
                selectedConversation.value = conversation
                conversationTitleInput.value = conversation.title
            }
        }
    }

    fun selectConversation(conversationId: String) {
        viewModelScope.launch {
            val conversation = repository.getConversation(conversationId) ?: return@launch
            selectedConversation.value = conversation
            conversationTitleInput.value = conversation.title
            aiInteractionState.value = AiInteractionState()
        }
    }

    fun createConversation() {
        val document = selectedDocument.value ?: return
        val title = nextConversationTitle(uiState.value.conversations)
        viewModelScope.launch {
            val conversation = repository.createConversation(
                documentId = document.id,
                title = title,
            )
            selectedConversation.value = conversation
            conversationTitleInput.value = conversation.title
            aiInteractionState.value = AiInteractionState()
        }
    }

    fun updateConversationTitle(title: String) {
        conversationTitleInput.value = title
    }

    fun renameSelectedConversation() {
        val conversation = selectedConversation.value ?: return
        val title = conversationTitleInput.value.trim()
        if (title.isBlank()) {
            statusMessage.value = "会话名称不能为空"
            return
        }
        viewModelScope.launch {
            repository.renameConversation(conversation.id, title)
            selectedConversation.value = conversation.copy(title = title)
            statusMessage.value = "已重命名会话"
        }
    }

    fun clearStatusMessage() {
        statusMessage.value = null
    }

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun checkBackendHealth() {
        viewModelScope.launch {
            backendHealth.value = backendHealth.value.copy(
                status = "检测中",
                detail = "正在访问 ${backendHealth.value.baseUrl}health",
                isChecking = true,
            )
            runCatching {
                apiService.health()
            }.onSuccess { response ->
                backendHealth.value = if (response.status == "ok") {
                    backendHealth.value.copy(
                        status = "后端可用",
                        detail = "健康检查通过，可以发起摘要和问答请求。",
                        isChecking = false,
                    )
                } else {
                    backendHealth.value.copy(
                        status = "状态异常",
                        detail = "后端返回状态：${response.status}",
                        isChecking = false,
                    )
                }
            }.onFailure { throwable ->
                backendHealth.value = backendHealth.value.copy(
                    status = "连接失败",
                    detail = formatBackendHealthError(throwable),
                    isChecking = false,
                )
            }
        }
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
                statusMessage.value = formatAiRequestError(throwable)
            }
        }
    }

    fun updateQuestion(question: String) {
        aiInteractionState.value = aiInteractionState.value.copy(question = question)
    }

    fun askSelectedDocument() {
        val document = selectedDocument.value ?: return
        val question = aiInteractionState.value.question.trim()
        val conversation = selectedConversation.value
        if (question.isBlank()) {
            statusMessage.value = "请先输入问题"
            return
        }
        if (conversation == null) {
            statusMessage.value = "当前文档没有可用会话"
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
                val sources = response.sources.map { source ->
                    AiSourceUiItem(
                        chunkId = source.chunkId,
                        page = source.page,
                        quote = source.quote,
                    )
                }
                aiInteractionState.value = aiInteractionState.value.copy(
                    isAsking = false,
                    answer = response.answer,
                    sources = sources,
                    question = "",
                )
                persistQaMessages(
                    conversationId = conversation.id,
                    documentId = document.id,
                    question = question,
                    answer = response.answer,
                    sources = sources,
                )
            }.onFailure { throwable ->
                aiInteractionState.value = aiInteractionState.value.copy(isAsking = false)
                statusMessage.value = formatAiRequestError(throwable)
            }
        }
    }

    private suspend fun persistQaMessages(
        conversationId: String,
        documentId: String,
        question: String,
        answer: String,
        sources: List<AiSourceUiItem>,
    ) {
        val now = Instant.now()
        val userMessage = ConversationMessage(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            documentId = documentId,
            role = MessageRole.USER,
            content = question,
            createdAt = now,
        )
        val assistantMessageId = UUID.randomUUID().toString()
        val assistantMessage = ConversationMessage(
            id = assistantMessageId,
            conversationId = conversationId,
            documentId = documentId,
            role = MessageRole.ASSISTANT,
            content = answer,
            createdAt = now.plusMillis(1),
            sources = sources.map { source ->
                MessageSource(
                    id = UUID.randomUUID().toString(),
                    messageId = assistantMessageId,
                    chunkId = source.chunkId,
                    page = source.page,
                    quote = source.quote,
                )
            },
        )
        repository.addMessage(userMessage)
        repository.addMessage(assistantMessage)
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
    val sources: List<AiSourceUiItem> = emptyList(),
    val isSummarizing: Boolean = false,
    val isAsking: Boolean = false,
    val backendHealth: BackendHealthUiState = BackendHealthUiState(baseUrl = NetworkConfig.DEFAULT_BASE_URL),
    val conversationMessages: List<ConversationMessage> = emptyList(),
    val conversations: List<DocumentConversation> = emptyList(),
    val allConversations: List<ConversationListItem> = emptyList(),
    val selectedConversationId: String? = null,
    val conversationTitleInput: String = "",
)

private data class AiInteractionState(
    val question: String = "",
    val summary: String = "",
    val answer: String = "",
    val sources: List<AiSourceUiItem> = emptyList(),
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
