package com.arick.aiassistant

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.arick.aiassistant.ui.DocumentImportViewModel
import dagger.hilt.android.AndroidEntryPoint

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
            composable("chat") {
                ChatScreen(
                    uiState = uiState,
                    onConversationClick = { conversation ->
                        navController.navigate(
                            "detail/${conversation.documentId}?conversationId=${conversation.id}",
                        )
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
            composable(
                route = "detail/{documentId}?conversationId={conversationId}",
                arguments = listOf(
                    navArgument("conversationId") {
                        nullable = true
                        defaultValue = null
                    },
                ),
            ) { backStackEntry ->
                val documentId = backStackEntry.arguments?.getString("documentId").orEmpty()
                val conversationId = backStackEntry.arguments?.getString("conversationId")
                LaunchedEffect(documentId, conversationId) {
                    viewModel.loadDocument(documentId, conversationId)
                }
                DocumentDetailScreen(
                    navController = navController,
                    document = selectedDocument,
                    uiState = uiState,
                    onSummarizeClick = viewModel::summarizeSelectedDocument,
                    onQuestionChange = viewModel::updateQuestion,
                    onAskClick = viewModel::askSelectedDocument,
                    onConversationClick = viewModel::selectConversation,
                    onCreateConversationClick = viewModel::createConversation,
                    onConversationTitleChange = viewModel::updateConversationTitle,
                    onRenameConversationClick = viewModel::renameSelectedConversation,
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
            MainTab(route = "chat", label = "会话", icon = Icons.AutoMirrored.Filled.Chat),
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

private fun persistReadPermission(context: android.content.Context, uri: Uri) {
    runCatching {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION,
        )
    }
}
