package com.arick.aiassistant

import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CompactDocumentAction(
            icon = Icons.Filled.Search,
            contentDescription = searchLabel,
            onClick = onSearchClick,
        )
        CompactDocumentAction(
            icon = Icons.Filled.Add,
            contentDescription = importLabel,
            onClick = onImportClick,
        )
    }
}

@Composable
private fun CompactDocumentAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.size(36.dp),
        onClick = onClick,
        color = InkSurface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, InkBorder),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.padding(8.dp),
            tint = AssistantOrange,
        )
    }
}

@Composable
internal fun EmptyDocumentState(
    modifier: Modifier = Modifier,
) {
    AssistantPanel(modifier = modifier.fillMaxWidth()) {
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
                color = InkMuted,
            )
        }
    }
}

@Composable
internal fun DocumentCard(
    document: ImportedDocument,
    onClick: () -> Unit,
) {
    AssistantPanel(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                color = InkBlack,
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Description,
                    contentDescription = null,
                    modifier = Modifier.padding(11.dp),
                    tint = document.processingStatus.statusColor(),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    modifier = Modifier.padding(top = 4.dp),
                    text = "${document.type.displayName()} · ${document.createdAt.formatForDisplay()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val importNote = document.importNote
                if (!importNote.isNullOrBlank()) {
                    Text(
                        modifier = Modifier.padding(top = 6.dp),
                        text = importNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = AssistantOrange,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            StatusPill(
                text = document.processingStatus.displayName(),
                color = document.processingStatus.statusColor(),
            )
        }
    }
}

@Composable
internal fun DocumentProcessingStatus.statusColor() = when (this) {
    DocumentProcessingStatus.READY -> AssistantSuccess
    DocumentProcessingStatus.FAILED -> MaterialTheme.colorScheme.error
    DocumentProcessingStatus.IMPORTED -> AssistantOrange
    DocumentProcessingStatus.PARSING -> AssistantViolet
}

@Composable
internal fun DocumentType.iconTint() = when (this) {
    DocumentType.TEXT -> Color.White
    DocumentType.PDF -> AssistantSuccess
    DocumentType.IMAGE -> AssistantViolet
    DocumentType.OTHER -> InkMuted
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

internal fun Instant.formatForDisplay(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        .withZone(ZoneId.systemDefault())
    return formatter.format(this)
}
