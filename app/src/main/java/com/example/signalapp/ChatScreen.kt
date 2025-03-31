package com.example.signalapp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.* // 導入 collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // 導入 viewModel() 函數
import androidx.navigation.NavHostController
import com.example.signalapp.data.ChatMessage // 數據模型
import com.example.signalapp.ui.theme.SignalAppTheme
import com.example.signalapp.viewmodel.ChatViewModel
import com.example.signalapp.viewmodel.ChatUiState
import androidx.compose.material.icons.filled.CloudOff  // 導入斷線圖標
import androidx.compose.material3.LinearProgressIndicator // 顯示連接中或加載中
import androidx.compose.ui.text.style.TextAlign
import com.example.signalapp.viewmodel.ConnectionStatus // 導入連接狀態 Enum


// 模擬當前用戶 ID (需要與 ViewModel 中的一致)
// TODO: 將來會從 Authentication 模塊獲取
private const val currentUserId = "currentUser"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavHostController,
    viewModel: ChatViewModel = viewModel() // 獲取 ViewModel 實例
) {
    // --- 從 ViewModel 收集狀態 ---
    // 使用 collectAsState 將 StateFlow 轉換為 Compose State
    // 每當 uiState Flow 發出新值時，Compose 會自動重組使用 state 的部分
    val uiState by viewModel.uiState.collectAsState()

    SignalAppTheme {
        Scaffold(
            topBar = {
                Column { // 使用 Column 包裹 TopAppBar 和連接狀態提示
                    TopAppBar(
                        // 使用 ViewModel 中的 conversationName
                        title = { Text(uiState.conversationName) },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                            }
                        },

                        actions = {
                            // 可以在這裡顯示一個連接狀態圖標
                            when (uiState.connectionStatus) {
                                ConnectionStatus.CONNECTED -> { /* 可以不顯示圖標，或顯示一個 ✓ */
                                }

                                ConnectionStatus.CONNECTING -> {
                                    CircularProgressIndicator(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .padding(end = 8.dp),
                                        color = MaterialTheme.colorScheme.onPrimary, // 在 TopAppBar 上用對比色
                                        strokeWidth = 2.dp
                                    )
                                }

                                ConnectionStatus.DISCONNECTED, ConnectionStatus.ERROR -> {
                                    Icon(
                                        Icons.Filled.CloudOff, // 斷線圖標
                                        contentDescription = "未連接",
                                        tint = MaterialTheme.colorScheme.onErrorContainer, // 使用警告色
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            // ... (顏色設置不變) ...
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )

                    // --- 在 TopAppBar 下方顯示條狀提示 ---
                    ConnectionStatusBar(uiState = uiState)
                }
            },
            bottomBar = {
                ChatInput(
                    textValue = uiState.inputText, // 使用 ViewModel 中的 inputText
                    onTextChange = { viewModel.updateTextField(it) }, // 調用 ViewModel 更新輸入
                    onSendClick = { viewModel.sendMessage() }       // 調用 ViewModel 發送消息
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // <-- 1. 先應用 Scaffold 提供的 padding
                // .padding(top = 4.dp)    // <-- 2. 再額外添加你需要的 padding (如果需要的話)
                // .align(Alignment.Center) // 如果你需要居中
            ) { // 應用頂部 padding

                when {
                    // 初始加載（且未連接完成時顯示）
                    uiState.isLoading && uiState.messages.isEmpty() && uiState.connectionStatus == ConnectionStatus.CONNECTING -> {
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)) {
                            CircularProgressIndicator()
                            Text(
                                "正在連接並加載訊息...",
                                modifier = Modifier
                                    .padding(top = 70.dp)
                                    .align(Alignment.Center)
                            )
                        }
                    }
                    // 連接錯誤或數據加載錯誤
                    uiState.connectionStatus == ConnectionStatus.ERROR || (uiState.error != null && uiState.connectionStatus != ConnectionStatus.DISCONNECTED) -> { // 如果是斷開連接提示，優先顯示那個
                        Box(modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.Center)) {
                            Text(
                                "錯誤: ${uiState.error ?: "發生未知錯誤"}",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                    // 連接已斷開，但之前可能已加載部分消息
                    uiState.connectionStatus == ConnectionStatus.DISCONNECTED && uiState.error == "連接已斷開" -> {
                        // 顯示消息列表（如果有），並在頂部提示斷開
                        Column(modifier = Modifier.fillMaxSize()) {
                            // ConnectionStatusBar 已經在頂部顯示了
                            MessageList(
                                messages = uiState.messages,
                                currentUserId = currentUserId, // 使用正確的用戶ID
                                modifier = Modifier.fillMaxSize() // MessageList 會處理自己的 padding
                                // modifier = Modifier.weight(1f) // 讓列表填滿剩餘空
                            )
                        }
                    }
                    // 正常顯示消息列表
                    else -> {
                        MessageList(
                            messages = uiState.messages,
                            currentUserId = currentUserId, // 使用正確的用戶ID
                            modifier = Modifier.fillMaxSize() // MessageList 會處理自己的 padding
                        )
                    }
                }

                // 計算底部 padding，防止被輸入框遮擋
               // Spacer(modifier = Modifier.height(paddingValues.calculateBottomPadding()))
            }
        }
    }
}

// --- 新增一個顯示連接狀態的 Composable ---
@Composable
fun ConnectionStatusBar(uiState: ChatUiState) {
    when (uiState.connectionStatus) {
        ConnectionStatus.CONNECTING -> {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp), // 細一點的條
                color = MaterialTheme.colorScheme.secondary // 用不同顏色表示連接中
            )
            // 或者顯示文字：
            // Surface(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.secondary) {
            //     Text("正在連接...", modifier = Modifier.padding(4.dp), color = MaterialTheme.colorScheme.onSecondary, textAlign = TextAlign.Center)
            // }
        }

        ConnectionStatus.DISCONNECTED -> {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) { // 用柔和的背景色
                Text(
                    "⚠️ 連接已斷開，訊息可能延遲",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant, // 匹配背景
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }

        ConnectionStatus.ERROR -> {
            if (uiState.error != null && uiState.error != "連接已斷開") { // 只顯示連接/數據庫錯誤，非發送錯誤
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        "錯誤: ${uiState.error}",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }

        ConnectionStatus.CONNECTED -> {
            // 連接成功時不顯示任何狀態欄
            Spacer(modifier = Modifier.height(0.dp)) // 佔位，避免佈局跳動
        }
    }
}

@Composable
fun MessageList(
    messages: List<ChatMessage>,
    currentUserId: String, // <--- 添加 currentUserId 參數
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 8.dp, vertical = 8.dp),
        reverseLayout = true
        // state = rememberLazyListState() // 考慮添加 ListState 以便控制滾動到底部
    ) {
        items(
            items = messages, // messages 應該已經由 ViewModel 按時間排好序了
            key = { message -> message.id }
        ) { message ->
            // 將 currentUserId 傳遞給 MessageItem
            MessageItem(message = message, currentUserId = currentUserId)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun MessageItem(
    message: ChatMessage,
    currentUserId: String // <--- 添加 currentUserId 參數
) {
    // --- 在這裡判斷訊息是發出還是接收 ---
    val isSentByCurrentUser = message.senderId == currentUserId

    val horizontalArrangement = if (isSentByCurrentUser) Arrangement.End else Arrangement.Start
    val bubbleColor =
        if (isSentByCurrentUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val textColor =
        if (isSentByCurrentUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = bubbleColor,
            tonalElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                // (可選) 顯示發送者名字 (如果不是自己發的)
                if (!isSentByCurrentUser) {
                    Text(
                        text = message.senderId, // TODO: 將來用 senderName 替換
                        style = MaterialTheme.typography.labelSmall, // 小字體
                        color = MaterialTheme.colorScheme.primary // 或其他顏色
                    )
                }
                Text(
                    text = message.text,
                    color = textColor
                )
                // (可選) 顯示時間戳
                // Text(text = formatTimestamp(message.timestamp), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

// 修改 ChatInput 以接收 String 而不是 TextFieldValue (如果 VM 中管理 String)
@Composable
fun ChatInput(
    textValue: String, // 改為接收 String
    onTextChange: (String) -> Unit, // 回調參數改為 String
    onSendClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textValue, // 綁定 String
                onValueChange = onTextChange, // 綁定回調
                modifier = Modifier.weight(1f),
                placeholder = { Text("輸入訊息...") },
                shape = MaterialTheme.shapes.extraLarge
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onSendClick,
                enabled = textValue.isNotBlank() // 根據 String 判斷是否啟用
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "發送訊息",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

