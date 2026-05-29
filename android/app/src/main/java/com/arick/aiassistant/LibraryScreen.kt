package com.arick.aiassistant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arick.aiassistant.core.model.ImportedDocument
import com.arick.aiassistant.ui.DocumentImportUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LibraryScreen(
    uiState: DocumentImportUiState,
    onImportClick: () -> Unit,
    onDocumentClick: (ImportedDocument) -> Unit,
    onSearchClick: () -> Unit,
) {
    Scaffold(
        containerColor = InkBlack,
        contentWindowInsets = WindowInsets(0.dp),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(start = 20.dp, top = 0.dp, end = 20.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CompactPageHeader(
                    modifier = Modifier.weight(1f),
                    title = "文档库",
                    eyebrow = "DOCUMENTS",
                    subtitle = "管理本地导入资料",
                )
                TopBarDocumentActions(
                    importLabel = "导入",
                    searchLabel = "搜索",
                    onImportClick = onImportClick,
                    onSearchClick = onSearchClick,
                )
            }
            if (uiState.isImporting) {
                CircularProgressIndicator(color = AssistantOrange)
            }
            Text(
                text = "全部文档 ${uiState.documents.size}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (uiState.documents.isEmpty()) {
                EmptyDocumentState(
                    modifier = Modifier.padding(top = 12.dp),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(uiState.documents, key = { it.id }) { document ->
                        DocumentCard(
                            document = document,
                            onClick = { onDocumentClick(document) },
                        )
                    }
                }
            }
        }
    }
}
