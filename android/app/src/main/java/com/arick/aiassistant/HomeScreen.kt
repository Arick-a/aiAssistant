package com.arick.aiassistant

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.arick.aiassistant.core.model.ImportedDocument
import com.arick.aiassistant.ui.DocumentImportUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    uiState: DocumentImportUiState,
    onImportClick: () -> Unit,
    onSearchClick: () -> Unit,
    onDocumentClick: (ImportedDocument) -> Unit,
) {
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val maxCollapseOffsetPx = with(density) { 320.dp.toPx() }
    var collapseOffsetPx by remember { mutableFloatStateOf(0f) }
    val collapseProgress by remember {
        derivedStateOf {
            (collapseOffsetPx / maxCollapseOffsetPx).coerceIn(0f, 1f)
        }
    }
    val expandedHeaderHeight = 420.dp * (1f - collapseProgress).coerceIn(0f, 1f)
    val compactAlpha = ((collapseProgress - 0.35f) / 0.65f).coerceIn(0f, 1f)
    val compactHeaderHeight = 44.dp * compactAlpha
    val expandedAlpha = (1f - collapseProgress * 1.2f).coerceIn(0f, 1f)
    val nestedScrollConnection = remember(maxCollapseOffsetPx) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: androidx.compose.ui.input.nestedscroll.NestedScrollSource): Offset {
                val previousOffset = collapseOffsetPx
                val nextOffset = (previousOffset - available.y).coerceIn(0f, maxCollapseOffsetPx)
                collapseOffsetPx = nextOffset
                return if (nextOffset != previousOffset) Offset(x = 0f, y = available.y) else Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                val targetOffset = when {
                    available.y < -80f -> maxCollapseOffsetPx
                    available.y > 80f -> 0f
                    collapseOffsetPx > maxCollapseOffsetPx * 0.14f -> maxCollapseOffsetPx
                    else -> 0f
                }
                if (targetOffset != collapseOffsetPx) {
                    Animatable(collapseOffsetPx).animateTo(
                        targetValue = targetOffset,
                        animationSpec = tween(durationMillis = 220),
                    ) {
                        collapseOffsetPx = value
                    }
                }
                return Velocity.Zero
            }
        }
    }

    Scaffold(
        containerColor = InkBlack,
        contentWindowInsets = WindowInsets(0.dp),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
                .padding(innerPadding)
                .padding(start = 18.dp, top = 0.dp, end = 18.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(compactHeaderHeight)
                    .clipToBounds()
                    .graphicsLayer { alpha = compactAlpha },
                contentAlignment = Alignment.Center,
            ) {
                HomeTitleBar(
                    onSearchClick = onSearchClick,
                    onImportClick = onImportClick,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(expandedHeaderHeight)
                    .clipToBounds(),
            ) {
                Column(
                    modifier = Modifier.graphicsLayer {
                        alpha = expandedAlpha
                        translationY = -24.dp.toPx() * collapseProgress
                    },
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SectionHeader(
                        eyebrow = "LOCAL KNOWLEDGE",
                        title = "文档助手",
                        subtitle = "把文件导入本地后，可查看 OCR 文本、生成摘要，并通过搜索或问答定位来源片段。",
                    )
                    HomeOverviewPanel(uiState = uiState)
                    SearchAndImportActions(
                        onSearchClick = onSearchClick,
                        onImportClick = onImportClick,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "最近文档",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${uiState.documents.size} 份",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted,
                )
            }
            if (uiState.isImporting) {
                CircularProgressIndicator(color = AssistantOrange)
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clipToBounds(),
                state = listState,
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            )
            {
                if (uiState.documents.isEmpty()) {
                    item {
                        EmptyDocumentState()
                    }
                } else {
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
private fun HomeTitleBar(
    onSearchClick: () -> Unit,
    onImportClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "文档助手",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CompactIconAction(
                icon = Icons.Filled.Search,
                contentDescription = "搜索",
                onClick = onSearchClick,
            )
            CompactIconAction(
                icon = Icons.Filled.Add,
                contentDescription = "导入文件",
                onClick = onImportClick,
            )
        }
    }
}

@Composable
private fun CompactIconAction(
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
private fun HomeOverviewPanel(
    uiState: DocumentImportUiState,
) {
    val readyCount = uiState.documents.count { it.processingStatus.name == "READY" }
    val parsingCount = uiState.documents.count { it.processingStatus.name == "PARSING" }

    AssistantPanel {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(164.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Description,
                        contentDescription = null,
                        modifier = Modifier.size(30.dp),
                        tint = AssistantOrange,
                    )
                    Text(
                        text = if (uiState.documents.isEmpty()) {
                            "导入第一份资料开始建立本地知识库"
                        } else {
                            "今日处理 ${uiState.documents.size} 份文档"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                    )
                }
                Surface(
                    modifier = Modifier.size(width = 104.dp, height = 86.dp),
                    color = Color.White,
                    shape = RoundedCornerShape(4.dp),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        repeat(3) { index ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(if (index == 2) 0.58f else 1f)
                                    .height(7.dp)
                                    .background(if (index == 0) AssistantOrange else InkSubtle),
                            )
                        }
                    }
                }
            }
            AssistantMetricStrip {
                MetricText(
                    modifier = Modifier.weight(1f),
                    value = uiState.documents.size.toString(),
                    label = "本地文档",
                )
                MetricText(
                    modifier = Modifier.weight(1f),
                    value = readyCount.toString(),
                    label = "可提问",
                )
                MetricText(
                    modifier = Modifier.weight(1f),
                    value = parsingCount.toString(),
                    label = "处理中",
                )
            }
        }
    }
}

@Composable
private fun SearchAndImportActions(
    onSearchClick: () -> Unit,
    onImportClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SearchEntry(onClick = onSearchClick)
        ImportFileAction(onClick = onImportClick)
    }
}

@Composable
private fun SearchEntry(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        onClick = onClick,
        color = InkSurface,
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, InkBorder),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = AssistantOrange,
                modifier = Modifier.size(22.dp),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Text(
                    text = "搜索文档或片段",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "按关键词在本地 OCR 文本中检索",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted,
                    maxLines = 1,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = InkSubtle,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun ImportFileAction(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp),
        onClick = onClick,
        color = Color(0xFF291C0F),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, AssistantOrange),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(34.dp),
                color = AssistantOrange,
                shape = RoundedCornerShape(17.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.padding(7.dp),
                    tint = InkBlack,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = "导入文件",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "支持图片、PDF、TXT、Markdown",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkMuted,
                    maxLines = 1,
                )
            }
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = AssistantOrange,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
