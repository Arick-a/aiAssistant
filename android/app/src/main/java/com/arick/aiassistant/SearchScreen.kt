package com.arick.aiassistant

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.arick.aiassistant.core.model.SearchResult
import com.arick.aiassistant.ui.DocumentImportUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SearchScreen(
    uiState: DocumentImportUiState,
    onQueryChange: (String) -> Unit,
    onResultClick: (SearchResult) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("关键词搜索") })
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("输入关键词") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            )
            Text(
                modifier = Modifier.padding(top = 12.dp),
                text = "基于本地提取文本做关键词检索，当前为内存切块搜索。",
                style = MaterialTheme.typography.bodyMedium,
            )
            when {
                uiState.searchQuery.isBlank() -> {
                    EmptySearchState(
                        modifier = Modifier.padding(top = 24.dp),
                        text = "输入关键词后开始搜索。",
                    )
                }
                uiState.searchResults.isEmpty() -> {
                    EmptySearchState(
                        modifier = Modifier.padding(top = 24.dp),
                        text = "没有找到命中片段。",
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 20.dp),
                        contentPadding = PaddingValues(bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(uiState.searchResults, key = { "${it.documentId}-${it.chunkIndex}" }) { result ->
                            SearchResultCard(
                                result = result,
                                query = uiState.searchQuery,
                                onClick = { onResultClick(result) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptySearchState(
    modifier: Modifier = Modifier,
    text: String,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = text,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun SearchResultCard(
    result: SearchResult,
    query: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = result.documentTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                modifier = Modifier.padding(top = 6.dp),
                text = "片段 ${result.chunkIndex + 1}  命中 ${result.matchCount} 次",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                modifier = Modifier.padding(top = 10.dp),
                text = buildSnippet(result.snippet, query),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private fun buildSnippet(snippet: String, query: String): String {
    if (query.isBlank()) return snippet
    val lowerSnippet = snippet.lowercase()
    val lowerQuery = query.lowercase()
    val index = lowerSnippet.indexOf(lowerQuery)
    if (index < 0) return snippet
    val start = maxOf(0, index - 40)
    val end = minOf(snippet.length, index + query.length + 80)
    val prefix = if (start > 0) "..." else ""
    val suffix = if (end < snippet.length) "..." else ""
    return prefix + snippet.substring(start, end).replace('\n', ' ') + suffix
}
