package com.arick.aiassistant

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arick.aiassistant.core.model.ConversationListItem
import com.arick.aiassistant.ui.DocumentImportUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChatScreen(
    uiState: DocumentImportUiState,
    onConversationClick: (ConversationListItem) -> Unit,
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
        ) {
            CompactPageHeader(
                title = "会话",
                eyebrow = "ASK HISTORY",
                subtitle = "继续单文档问答",
            )
            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = "全部会话 ${uiState.allConversations.size}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (uiState.allConversations.isEmpty()) {
                AssistantPanel(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                ) {
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = "还没有会话。打开一份文档并提问后，会话会出现在这里。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InkMuted,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 12.dp),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(uiState.allConversations, key = { it.id }) { conversation ->
                        ConversationListCard(
                            conversation = conversation,
                            onClick = { onConversationClick(conversation) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationListCard(
    conversation: ConversationListItem,
    onClick: () -> Unit,
) {
    AssistantPanel(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = conversation.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                modifier = Modifier.padding(top = 6.dp),
                text = conversation.documentTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = AssistantOrange,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                modifier = Modifier.padding(top = 6.dp),
                text = conversation.latestMessagePreview ?: "还没有消息",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = "更新时间: ${conversation.updatedAt.formatForDisplay()}",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
