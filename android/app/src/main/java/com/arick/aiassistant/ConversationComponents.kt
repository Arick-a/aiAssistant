package com.arick.aiassistant

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arick.aiassistant.core.model.ConversationMessage
import com.arick.aiassistant.core.model.DocumentConversation
import com.arick.aiassistant.core.model.MessageRole

@Composable
internal fun ConversationSelector(
    modifier: Modifier = Modifier,
    conversations: List<DocumentConversation>,
    selectedConversationId: String?,
    titleInput: String,
    onConversationClick: (String) -> Unit,
    onCreateConversationClick: () -> Unit,
    onTitleChange: (String) -> Unit,
    onRenameClick: () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "会话",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            TextButton(onClick = onCreateConversationClick) {
                Text("新建")
            }
        }
        if (conversations.isNotEmpty()) {
            conversations.forEach { conversation ->
                TextButton(
                    onClick = { onConversationClick(conversation.id) },
                ) {
                    Text(
                        text = if (conversation.id == selectedConversationId) {
                            "当前：${conversation.title}"
                        } else {
                            conversation.title
                        },
                    )
                }
            }
        }
        OutlinedTextField(
            value = titleInput,
            onValueChange = onTitleChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            singleLine = true,
            label = { Text("会话名称") },
        )
        Button(
            modifier = Modifier.padding(top = 8.dp),
            enabled = selectedConversationId != null,
            onClick = onRenameClick,
        ) {
            Text("保存名称")
        }
    }
}

@Composable
internal fun ConversationMessageItem(
    modifier: Modifier = Modifier,
    message: ConversationMessage,
) {
    val isUser = message.role == MessageRole.USER
    val bubbleColor = if (isUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val horizontalAlignment = if (isUser) Alignment.End else Alignment.Start

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = horizontalAlignment,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.86f)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp,
                    ),
                )
                .background(bubbleColor)
                .padding(12.dp),
        ) {
            Text(
                text = if (isUser) "我" else "AI",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                modifier = Modifier.padding(top = 6.dp),
                text = message.content,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (message.sources.isNotEmpty()) {
                Text(
                    modifier = Modifier.padding(top = 10.dp),
                    text = "来源片段",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                )
                message.sources.forEachIndexed { index, source ->
                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = "${index + 1}. ${source.chunkId}${source.page?.let { " · 第 $it 页" } ?: ""}：${source.quote}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}
