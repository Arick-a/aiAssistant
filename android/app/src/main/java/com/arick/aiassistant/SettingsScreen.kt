package com.arick.aiassistant

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.arick.aiassistant.ui.BackendHealthUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsScreen(
    health: BackendHealthUiState,
    onCheckBackendClick: () -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            CompactPageHeader(
                title = "设置",
                eyebrow = "SYSTEM",
                subtitle = "服务连接与实验配置",
            )
            BackendStatusCard(
                health = health,
                onCheckClick = onCheckBackendClick,
            )
        }
    }
}

@Composable
private fun BackendStatusCard(
    modifier: Modifier = Modifier,
    health: BackendHealthUiState,
    onCheckClick: () -> Unit,
) {
    AssistantPanel(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "后端连接",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Button(
                    enabled = !health.isChecking,
                    onClick = onCheckClick,
                ) {
                    Text(if (health.isChecking) "检测中" else "检测")
                }
            }
            Text(
                modifier = Modifier.padding(top = 10.dp),
                text = health.status,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = health.detail,
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted,
            )
            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = health.baseUrl,
                style = MaterialTheme.typography.bodySmall,
                color = InkMuted,
            )
        }
    }
}
