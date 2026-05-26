package com.arick.aiassistant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arick.aiassistant.core.model.DocumentProcessingStatus
import com.arick.aiassistant.core.model.ImportedDocument
import com.arick.aiassistant.ui.DocumentImportUiState

@Composable
internal fun DocumentSummaryTab(
    document: ImportedDocument,
    uiState: DocumentImportUiState,
    onSummarizeClick: () -> Unit,
) {
    val isReady = document.processingStatus == DocumentProcessingStatus.READY
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AI 摘要",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Button(
                        modifier = Modifier.padding(top = 12.dp),
                        enabled = !uiState.isSummarizing && isReady,
                        onClick = onSummarizeClick,
                    ) {
                        Text(if (uiState.isSummarizing) "生成中..." else "生成摘要")
                    }
                    if (!isReady) {
                        Text(
                            modifier = Modifier.padding(top = 10.dp),
                            text = "文档${document.processingStatus.displayName()}，暂不能生成摘要。",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (uiState.isSummarizing) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    }
                    if (uiState.summary.isNotBlank()) {
                        Text(
                            modifier = Modifier.padding(top = 12.dp),
                            text = uiState.summary,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}
