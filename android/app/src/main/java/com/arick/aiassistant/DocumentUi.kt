package com.arick.aiassistant

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arick.aiassistant.core.model.DocumentType
import com.arick.aiassistant.core.model.DocumentProcessingStatus
import com.arick.aiassistant.core.model.ImportedDocument
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
internal fun TopBarDocumentActions(
    importLabel: String,
    searchLabel: String,
    onImportClick: () -> Unit,
    onSearchClick: () -> Unit,
) {
    Row {
        TextButton(onClick = onImportClick) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(importLabel)
        }
        TextButton(onClick = onSearchClick) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(searchLabel)
        }
    }
}

@Composable
internal fun EmptyDocumentState(
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "还没有文档",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = "先导入一份 txt、md、pdf 或图片文件，验证导入链路是否正常。",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
internal fun DocumentCard(
    document: ImportedDocument,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = document.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                modifier = Modifier.padding(top = 6.dp),
                text = "类型: ${document.type.displayName()}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = "状态: ${document.processingStatus.displayName()}",
                style = MaterialTheme.typography.bodySmall,
                color = document.processingStatus.statusColor(),
            )
            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = "导入时间: ${document.createdAt.formatForDisplay()}",
                style = MaterialTheme.typography.bodySmall,
            )
            val importNote = document.importNote
            if (!importNote.isNullOrBlank()) {
                Text(
                    modifier = Modifier.padding(top = 8.dp),
                    text = importNote,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

internal fun DocumentType.displayName(): String = when (this) {
    DocumentType.TEXT -> "文本"
    DocumentType.PDF -> "PDF"
    DocumentType.IMAGE -> "图片"
    DocumentType.OTHER -> "其他"
}

internal fun DocumentProcessingStatus.displayName(): String = when (this) {
    DocumentProcessingStatus.IMPORTED -> "已导入"
    DocumentProcessingStatus.PARSING -> "解析中"
    DocumentProcessingStatus.READY -> "可提问"
    DocumentProcessingStatus.FAILED -> "解析失败"
}

@Composable
private fun DocumentProcessingStatus.statusColor() = when (this) {
    DocumentProcessingStatus.READY -> MaterialTheme.colorScheme.primary
    DocumentProcessingStatus.FAILED -> MaterialTheme.colorScheme.error
    DocumentProcessingStatus.IMPORTED,
    DocumentProcessingStatus.PARSING -> MaterialTheme.colorScheme.onSurfaceVariant
}

internal fun Instant.formatForDisplay(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        .withZone(ZoneId.systemDefault())
    return formatter.format(this)
}
