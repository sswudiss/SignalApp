package com.example.signalapp.ui.chatlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddComment // 或者 Message, Edit 等圖標
import androidx.compose.material3.* // 導入 M3 組件
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.signalapp.ui.theme.SignalAppTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.signalapp.navigation.AppDestinations


// 註: @OptIn annotation 可能需要，如果使用的 M3 API 是實驗性的
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    navController: NavController,
    viewModel: ChatListViewModel = hiltViewModel()
) {

    // --- 從 ViewModel 收集狀態 ---
    val uiState by viewModel.uiState.collectAsState()

    // Scaffold 提供了標準 Material Design 佈局結構的插槽
    Scaffold(
        topBar = {
            // 頂部應用欄
            TopAppBar(
                title = { Text("聊天") }, // 或者用你的應用名稱
                colors = TopAppBarDefaults.topAppBarColors( // 可選: 自定義顏色
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
                // 可以添加導航圖標或操作按鈕
                // navigationIcon = { ... }
                // actions = { ... }
            )
        },
        floatingActionButton = {
            // 懸浮操作按鈕
            FloatingActionButton(
                onClick = {
                    navController.navigate(AppDestinations.NEW_CHAT_ROUTE)
                }
            ) {
                Icon(Icons.Filled.AddComment, contentDescription = "開始新聊天")
            }
        }
        // 可以配置 FAB 的位置，默認是右下角
        // floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
// --- 根據狀態顯示不同內容 ---
        when {
            // 1. 正在加載
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            // 2. 加載出錯
            uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "錯誤: ${uiState.errorMessage}",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // 可以添加一個重試按鈕
                    Button(onClick = { /* viewModel.refreshChatList() */ }) { // 需要 ViewModel 提供刷新方法
                        Text("重試")
                    }
                }
            }
            // 3. 列表為空
            uiState.chats.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "還沒有聊天記錄",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            // 4. 顯示聊天列表
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    items(
                        items = uiState.chats, // <--- 使用 ViewModel 中的列表
                        key = { chat -> chat.id }
                    ) { chatSummary ->
                        ChatItem(
                            chatSummary = chatSummary,
                            onClick = { chatId ->
                                navController.navigate(AppDestinations.chatDetailPath(chatId))
                                // 或者直接拼接字符串，但輔助函數更安全:
                                // navController.navigate("${AppDestinations.CHAT_DETAIL_ROUTE}/$chatId")
                            }
                        )
                        Divider(
                            modifier = Modifier.padding(start = 16.dp + 50.dp + 16.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        } // end of when
    }
}
