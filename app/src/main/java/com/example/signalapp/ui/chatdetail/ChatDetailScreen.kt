package com.example.signalapp.ui.chatdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // Use auto-mirrored icon
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.signalapp.ui.theme.SignalAppTheme
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.rememberLazyListState // 導入 list state
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class) // TopAppBar 可能需要
@Composable
fun ChatDetailScreen(
    // 不再需要傳遞 chatId，ViewModel 會從 SavedStateHandle 獲取
    // chatId: String,
    onNavigateBack: () -> Unit, // 導航返回的回調
    viewModel: ChatDetailViewModel = hiltViewModel() // 通過 Hilt 獲取 ViewModel
) {

    // 從 ViewModel 收集 UI 狀態
    val uiState by viewModel.uiState.collectAsState()
    // 創建 LazyColumn 的狀態以便控制滾動
    val listState = rememberLazyListState()
    // 獲取 CoroutineScope 用於啟動滾動協程
    val coroutineScope = rememberCoroutineScope()

    // 使用 LaunchedEffect 在消息列表大小變化時自動滾動到底部
    // key 設置為列表大小，當大小變化（新消息）時觸發
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            // 由於使用 reverseLayout=true，滾動到索引 0 就是滾動到視覺上的底部
            coroutineScope.launch {
                // 可以稍微延遲一點點，確保列表渲染完成
                // delay(50)
                listState.animateScrollToItem(0) // 使用動畫滾動更平滑
                // listState.scrollToItem(0) // 或者直接跳轉
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // 顯示從 ViewModel 獲取的聊天夥伴名稱，如果為空則顯示默認值
                    Text(uiState.chatPartnerName.ifEmpty { "聊天" })
                },
                navigationIcon = {
                    // 返回按鈕觸發傳入的 onNavigateBack 回調
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
                // 可以在這裡添加其他按鈕，如呼叫、視頻通話等
            )
        },
        // 不設置 bottomBar，輸入框作為 Column 的一部分處理鍵盤遮擋
    ) { innerPadding -> // Scaffold 提供內邊距，主要用於處理 topBar 的高度

        // 使用 Column 垂直排列消息列表和輸入框
        Column(
            modifier = Modifier
                .fillMaxSize()
                // 只應用 Scaffold 提供的頂部內邊距
                // 底部的鍵盤邊距由 MessageInput 內部的 imePadding 處理
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            // 消息列表區域
            Box(
                modifier = Modifier
                    .weight(1f) // 佔據 Column 中除了輸入框外的所有可用空間
                    .fillMaxWidth() // 確保橫向填滿
            ) {
                when {
                    // 狀態 1: 正在加載初始消息
                    uiState.isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    // 狀態 2: 加載出錯
                    uiState.errorMessage != null -> {
                        Text(
                            text = "錯誤: ${uiState.errorMessage}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(16.dp)
                        )
                    }
                    // 狀態 3: 加載完成，顯示消息列表 (即使列表可能為空)
                    else -> {
                        LazyColumn(
                            state = listState, // 綁定列表狀態
                            modifier = Modifier.fillMaxSize(), // 填滿 Box 空間
                            reverseLayout = true // *** 關鍵：讓列表從底部開始顯示，新的在下面 ***
                        ) {
                            // 使用 items 遍歷從 uiState 獲取的消息列表
                            items(
                                items = uiState.messages,
                                key = { message -> message.id } // 使用唯一消息 ID 作為 key
                            ) { message ->
                                // 為每條消息渲染 ChatMessageItem
                                ChatMessageItem(message = message)
                            }
                        } // End LazyColumn
                    }
                } // End when
            } // End Box for list area

            // 輸入框區域
            MessageInput(
                // 綁定 ViewModel 中的輸入文本狀態
                currentText = uiState.currentInput,
                // 當輸入變化時，通知 ViewModel 更新狀態
                onInputChange = { viewModel.updateInput(it) },
                // 當點擊發送時，通知 ViewModel 處理
                onSendMessage = { viewModel.sendMessage() },
                // Modifier 可以用來添加額外的邊距或樣式（如果需要）
                modifier = Modifier // .padding(bottom = innerPadding.calculateBottomPadding()) // 如果 Scaffold 有 bottomBar 才需要考慮這個
            )

        } // End Column
    } // End Scaffold
}

// --- 預覽 ---
@Preview(showBackground = true)
@Composable
fun ChatDetailScreenPreview() {
    SignalAppTheme {
        // 由於依賴 Hilt ViewModel 和導航參數，直接預覽意義不大
        // 最好是創建一個接收 ChatDetailUiState 的假 Content Composable 來預覽
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("ChatDetailScreen Preview (Needs ViewModel Interaction)")
        }
    }
}