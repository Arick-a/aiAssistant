package com.arick.aiassistant

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.arick.aiassistant.core.model.ImportedDocument
import com.arick.aiassistant.ui.DocumentImportUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun DocumentDetailScreen(
    navController: NavHostController,
    document: ImportedDocument?,
    uiState: DocumentImportUiState,
    onSummarizeClick: () -> Unit,
    onQuestionChange: (String) -> Unit,
    onAskClick: () -> Unit,
    onConversationClick: (String) -> Unit,
    onCreateConversationClick: () -> Unit,
    onConversationTitleChange: (String) -> Unit,
    onRenameConversationClick: () -> Unit,
) {
    val tabs = listOf("概览", "摘要", "问答", "原文")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(document?.title ?: "文档详情") },
            )
        },
    ) { innerPadding ->
        if (document == null) {
            MissingDocumentContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                onBackClick = { navController.popBackStack() },
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(title) },
                    )
                }
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                when (page) {
                    0 -> DocumentOverviewTab(document = document)
                    1 -> DocumentSummaryTab(
                        document = document,
                        uiState = uiState,
                        onSummarizeClick = onSummarizeClick,
                    )
                    2 -> DocumentQaTab(
                        document = document,
                        uiState = uiState,
                        onQuestionChange = onQuestionChange,
                        onAskClick = onAskClick,
                        onConversationClick = onConversationClick,
                        onCreateConversationClick = onCreateConversationClick,
                        onConversationTitleChange = onConversationTitleChange,
                        onRenameConversationClick = onRenameConversationClick,
                    )
                    3 -> DocumentRawTextTab(document = document)
                }
            }
        }
    }
}

@Composable
private fun MissingDocumentContent(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
) {
    Column(
        modifier = modifier.padding(20.dp),
    ) {
        Text("文档不存在或已丢失。")
        Button(
            modifier = Modifier.padding(top = 16.dp),
            onClick = onBackClick,
        ) {
            Text("返回")
        }
    }
}
