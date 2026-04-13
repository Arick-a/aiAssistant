package com.arick.aiassistant.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arick.aiassistant.core.data.DocumentRepository
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
) : ViewModel() {
    private val importStatus = MutableStateFlow(false)
    private val statusMessage = MutableStateFlow<String?>(null)
    private val selectedDocument = MutableStateFlow<ImportedDocument?>(null)
    private val searchQuery = MutableStateFlow("")
    val selectedDocumentState: StateFlow<ImportedDocument?> = selectedDocument

    val uiState: StateFlow<DocumentImportUiState> = combine(
        repository.observeDocuments(),
        importStatus,
        statusMessage,
        searchQuery,
    ) { documents, isImporting, status, query ->
            DocumentImportUiState(
                documents = documents,
                isImporting = isImporting,
                statusMessage = status,
                searchQuery = query,
                searchResults = searchEngine.search(documents, query),
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
            selectedDocument.value = repository.getDocument(documentId)
        }
    }

    fun clearStatusMessage() {
        statusMessage.value = null
    }

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }
}

data class DocumentImportUiState(
    val documents: List<ImportedDocument> = emptyList(),
    val isImporting: Boolean = false,
    val statusMessage: String? = null,
    val searchQuery: String = "",
    val searchResults: List<SearchResult> = emptyList(),
)
