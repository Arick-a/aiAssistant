package com.arick.aiassistant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arick.aiassistant.core.model.ImportedDocument

@Composable
internal fun DocumentOverviewTab(
    document: ImportedDocument,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = document.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        modifier = Modifier.padding(top = 8.dp),
                        text = "类型: ${document.type.displayName()}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = "状态: ${document.processingStatus.displayName()}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = "导入时间: ${document.createdAt.formatForDisplay()}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = "URI: ${document.uri}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    document.importNote?.let { note ->
                        Text(
                            modifier = Modifier.padding(top = 10.dp),
                            text = note,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}
