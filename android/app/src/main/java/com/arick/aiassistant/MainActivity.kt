package com.arick.aiassistant

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.arick.aiassistant.core.model.DocumentType
import com.arick.aiassistant.core.model.ImportedDocument
import com.arick.aiassistant.core.model.SearchResult
import com.arick.aiassistant.ui.BackendHealthUiState
import com.arick.aiassistant.ui.DocumentImportUiState
import com.arick.aiassistant.ui.DocumentImportViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AiAssistantTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AppRoot()
                }
            }
        }
    }
}

@Composable
private fun AppRoot(
    viewModel: DocumentImportViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedDocument by viewModel.selectedDocumentState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        persistReadPermission(context, uri)
        viewModel.importDocument(uri)
    }

    LaunchedEffect(uiState.statusMessage) {
        val message = uiState.statusMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearStatusMessage()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (currentRoute in MainTab.routes) {
                MainBottomBar(
                    currentRoute = currentRoute,
                    onTabClick = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding),
        ) {
            composable("home") {
                HomeScreen(
                    uiState = uiState,
                    onImportClick = {
                        importLauncher.launch(arrayOf("*/*"))
                    },
                    onDocumentClick = { document ->
                        navController.navigate("detail/${document.id}")
                    },
                    onSearchClick = {
                        navController.navigate("search")
                    },
                )
            }
            composable("library") {
                LibraryScreen(
                    uiState = uiState,
                    onImportClick = {
                        importLauncher.launch(arrayOf("*/*"))
                    },
                    onDocumentClick = { document ->
                        navController.navigate("detail/${document.id}")
                    },
                    onSearchClick = {
                        navController.navigate("search")
                    },
                )
            }
            composable("settings") {
                SettingsScreen(
                    health = uiState.backendHealth,
                    onCheckBackendClick = viewModel::checkBackendHealth,
                )
            }
            composable("search") {
                SearchScreen(
                    uiState = uiState,
                    onQueryChange = viewModel::updateSearchQuery,
                    onResultClick = { result ->
                        navController.navigate("detail/${result.documentId}")
                    },
                )
            }
            composable("detail/{documentId}") { backStackEntry ->
                val documentId = backStackEntry.arguments?.getString("documentId").orEmpty()
                LaunchedEffect(documentId) {
                    viewModel.loadDocument(documentId)
                }
                DocumentDetailScreen(
                    navController = navController,
                    document = selectedDocument,
                    uiState = uiState,
                    onSummarizeClick = viewModel::summarizeSelectedDocument,
                    onQuestionChange = viewModel::updateQuestion,
                    onAskClick = viewModel::askSelectedDocument,
                )
            }
        }
    }
}

private data class MainTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    companion object {
        val items = listOf(
            MainTab(route = "home", label = "首页", icon = Icons.Filled.Home),
            MainTab(route = "library", label = "文档", icon = Icons.Filled.Folder),
            MainTab(route = "settings", label = "设置", icon = Icons.Filled.Settings),
        )
        val routes = items.map { it.route }.toSet()
    }
}

@Composable
private fun MainBottomBar(
    currentRoute: String?,
    onTabClick: (String) -> Unit,
) {
    NavigationBar {
        MainTab.items.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = { onTabClick(tab.route) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.label,
                    )
                },
                label = { Text(tab.label) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(
    uiState: DocumentImportUiState,
    onImportClick: () -> Unit,
    onDocumentClick: (ImportedDocument) -> Unit,
    onSearchClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Assistant") },
                actions = {
                    TopBarDocumentActions(
                        importLabel = "导入",
                        searchLabel = "搜索",
                        onImportClick = onImportClick,
                        onSearchClick = onSearchClick,
                    )
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            Text(
                text = "导入并提问你的文档",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = "当前支持文档导入、OCR、本地搜索、AI 摘要和单文档问答。",
                style = MaterialTheme.typography.bodyMedium,
            )
            if (uiState.isImporting) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
            Text(
                modifier = Modifier.padding(top = 24.dp),
                text = "最近文档",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (uiState.documents.isEmpty()) {
                EmptyState(
                    modifier = Modifier.padding(top = 12.dp),
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 12.dp),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(uiState.documents.take(5), key = { it.id }) { document ->
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

@Composable
private fun TopBarDocumentActions(
    importLabel: String,
    searchLabel: String,
    onImportClick: () -> Unit,
    onSearchClick: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryScreen(
    uiState: DocumentImportUiState,
    onImportClick: () -> Unit,
    onDocumentClick: (ImportedDocument) -> Unit,
    onSearchClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("文档库") },
                actions = {
                    TopBarDocumentActions(
                        importLabel = "导入",
                        searchLabel = "搜索",
                        onImportClick = onImportClick,
                        onSearchClick = onSearchClick,
                    )
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            if (uiState.isImporting) {
                CircularProgressIndicator()
            }
            Text(
                modifier = Modifier.padding(top = if (uiState.isImporting) 20.dp else 0.dp),
                text = "全部文档 ${uiState.documents.size}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (uiState.documents.isEmpty()) {
                EmptyState(
                    modifier = Modifier.padding(top = 12.dp),
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 12.dp),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreen(
    health: BackendHealthUiState,
    onCheckBackendClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("设置") })
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
        ) {
            BackendStatusCard(
                health = health,
                onCheckClick = onCheckBackendClick,
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "当前阶段",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        modifier = Modifier.padding(top = 8.dp),
                        text = "MVP 联调：导入、OCR、本地搜索、摘要、问答。",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun BackendStatusCard(
    modifier: Modifier = Modifier,
    health: BackendHealthUiState,
    onCheckClick: () -> Unit,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "后端状态：${health.status}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                modifier = Modifier.padding(top = 6.dp),
                text = health.baseUrl,
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = health.detail,
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(
                modifier = Modifier.padding(top = 12.dp),
                enabled = !health.isChecking,
                onClick = onCheckClick,
            ) {
                Text(if (health.isChecking) "检测中..." else "检测后端")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchScreen(
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

@Composable
private fun EmptyState(
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
private fun DocumentCard(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DocumentDetailScreen(
    navController: NavHostController,
    document: ImportedDocument?,
    uiState: DocumentImportUiState,
    onSummarizeClick: () -> Unit,
    onQuestionChange: (String) -> Unit,
    onAskClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(document?.title ?: "文档详情") },
            )
        },
    ) { innerPadding ->
        if (document == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp),
            ) {
                Text("文档不存在或已丢失。")
                Button(
                    modifier = Modifier.padding(top = 16.dp),
                    onClick = { navController.popBackStack() },
                ) {
                    Text("返回")
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
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
                            enabled = !uiState.isSummarizing && document.extractedText.isNotBlank(),
                            onClick = onSummarizeClick,
                        ) {
                            Text(if (uiState.isSummarizing) "生成中..." else "生成摘要")
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
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "基于文档提问",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        OutlinedTextField(
                            value = uiState.question,
                            onValueChange = onQuestionChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp),
                            label = { Text("输入你的问题") },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        )
                        Button(
                            modifier = Modifier.padding(top = 12.dp),
                            enabled = !uiState.isAsking && document.extractedText.isNotBlank(),
                            onClick = onAskClick,
                        ) {
                            Text(if (uiState.isAsking) "思考中..." else "提问")
                        }
                        if (uiState.isAsking) {
                            CircularProgressIndicator(
                                modifier = Modifier.padding(top = 12.dp),
                            )
                        }
                        if (uiState.answer.isNotBlank()) {
                            Text(
                                modifier = Modifier.padding(top = 12.dp),
                                text = uiState.answer,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        if (uiState.sources.isNotEmpty()) {
                            Text(
                                modifier = Modifier.padding(top = 12.dp),
                                text = "来源片段",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                            )
                            uiState.sources.forEachIndexed { index, source ->
                                Column(modifier = Modifier.padding(top = 8.dp)) {
                                    Text(
                                        text = "${index + 1}. ${source.displayLabel()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        modifier = Modifier.padding(top = 4.dp),
                                        text = source.quote,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "提取文本",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            modifier = Modifier.padding(top = 10.dp),
                            text = document.extractedText.ifBlank {
                                "当前文件类型暂未执行文本提取。这是下一步 OCR / PDF 解析接入点。"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

private fun persistReadPermission(context: android.content.Context, uri: Uri) {
    runCatching {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION,
        )
    }
}

private fun DocumentType.displayName(): String = when (this) {
    DocumentType.TEXT -> "文本"
    DocumentType.PDF -> "PDF"
    DocumentType.IMAGE -> "图片"
    DocumentType.OTHER -> "其他"
}

private fun java.time.Instant.formatForDisplay(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        .withZone(ZoneId.systemDefault())
    return formatter.format(this)
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

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    AiAssistantTheme {
        HomeScreen(
            uiState = DocumentImportUiState(
                documents = listOf(
                    ImportedDocument(
                        id = "1",
                        title = "sample.txt",
                        type = DocumentType.TEXT,
                        uri = "content://sample",
                        mimeType = "text/plain",
                        extractedText = "hello",
                        importNote = null,
                        createdAt = java.time.Instant.now(),
                    ),
                ),
            ),
            onImportClick = {},
            onDocumentClick = {},
            onSearchClick = {},
        )
    }
}
