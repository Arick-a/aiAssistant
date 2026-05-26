package com.arick.aiassistant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.arick.aiassistant.core.model.DocumentProcessingStatus
import com.arick.aiassistant.core.model.ImportedDocument
import com.arick.aiassistant.ui.DocumentImportUiState

@Composable
internal fun DocumentQaTab(
    document: ImportedDocument,
    uiState: DocumentImportUiState,
    onQuestionChange: (String) -> Unit,
    onAskClick: () -> Unit,
    onConversationClick: (String) -> Unit,
    onCreateConversationClick: () -> Unit,
    onConversationTitleChange: (String) -> Unit,
    onRenameConversationClick: () -> Unit,
) {
    val listState = rememberLazyListState()
    val isReady = document.processingStatus == DocumentProcessingStatus.READY
    LaunchedEffect(uiState.conversationMessages.size) {
        if (uiState.conversationMessages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.conversationMessages.size + 1)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            state = listState,
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                ConversationSetupCard(
                    uiState = uiState,
                    onConversationClick = onConversationClick,
                    onCreateConversationClick = onCreateConversationClick,
                    onConversationTitleChange = onConversationTitleChange,
                    onRenameConversationClick = onRenameConversationClick,
                )
            }
            if (uiState.conversationMessages.isNotEmpty()) {
                item {
                    Text(
                        text = "历史问答",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                items(uiState.conversationMessages, key = { it.id }) { message ->
                    ConversationMessageItem(message = message)
                }
            }
        }
        QaInputBar(
            question = uiState.question,
            isAsking = uiState.isAsking,
            canAsk = isReady,
            onQuestionChange = onQuestionChange,
            onAskClick = onAskClick,
        )
    }
}

@Composable
private fun ConversationSetupCard(
    uiState: DocumentImportUiState,
    onConversationClick: (String) -> Unit,
    onCreateConversationClick: () -> Unit,
    onConversationTitleChange: (String) -> Unit,
    onRenameConversationClick: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "基于文档提问",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            ConversationSelector(
                modifier = Modifier.padding(top = 12.dp),
                conversations = uiState.conversations,
                selectedConversationId = uiState.selectedConversationId,
                titleInput = uiState.conversationTitleInput,
                onConversationClick = onConversationClick,
                onCreateConversationClick = onCreateConversationClick,
                onTitleChange = onConversationTitleChange,
                onRenameClick = onRenameConversationClick,
            )
        }
    }
}

@Composable
private fun QaInputBar(
    question: String,
    isAsking: Boolean,
    canAsk: Boolean,
    onQuestionChange: (String) -> Unit,
    onAskClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = question,
                onValueChange = onQuestionChange,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                placeholder = { Text("输入你的问题") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                singleLine = true,
            )
            Button(
                modifier = Modifier
                    .width(80.dp)
                    .height(56.dp),
                enabled = !isAsking && canAsk,
                onClick = onAskClick,
            ) {
                Text(if (isAsking) "..." else "发送")
            }
        }
        if (isAsking) {
            CircularProgressIndicator(
                modifier = Modifier.padding(start = 16.dp, bottom = 12.dp),
            )
        }
    }
}
