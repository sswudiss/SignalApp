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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel // Import viewModel composable
import androidx.navigation.NavController // Assuming you might pass NavController later if needed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: ChatDetailViewModel = hiltViewModel() // 暫時註釋掉，先用假數據
) {

    // --- 收集 ViewModel 狀態 ---
    val uiState by viewModel.uiState.collectAsState()
    // Get participant name from state, using chatId from ViewModel as fallback
    val participantName = uiState.participantName.ifEmpty { "加載中..." }
    // (We can also get chatId directly from viewmodel if needed: val currentChatId by viewModel.chatId.collectAsState() )

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // --- 自動滾動邏輯 (現在監聽 ViewModel 中的 messages) ---
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            coroutineScope.launch { listState.scrollToItem(0) } // Still scroll to top (visual bottom) with reverseLayout
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(participantName) }, // Use name from UiState
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = { // Example: Add a refresh button
                    IconButton(onClick = { viewModel.refreshMessages() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "刷新"
                        ) // Use androidx.compose.material.icons.filled.Refresh
                    }
                }
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            // --- 根據狀態顯示不同內容 ---
            when {
                // 1. 加載中
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(), // Take up LazyColumn's space
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                    // Keep MessageInput visible but potentially disabled during initial load?
                    MessageInput(
                        onSendMessage = { /* NO-OP or disable */ },
                        modifier = Modifier/* ... */
                    )
                }
                // 2. 出錯
                uiState.errorMessage != null -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "錯誤: ${uiState.errorMessage}",
                            color = MaterialTheme.colorScheme.error
                        )
                        // Maybe add retry button here?
                    }
                    MessageInput(
                        onSendMessage = { /* NO-OP or disable */ },
                        modifier = Modifier/* ... */
                    )
                }
                // 3. 成功加載 (包括空消息列表)
                else -> {
                    // 消息列表區域
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        reverseLayout = true
                    ) {
                        items(
                            items = uiState.messages, // Use messages from UiState
                            key = { message -> message.id }
                        ) { message ->
                            ChatMessageItem(message = message)
                        }
                    } // end LazyColumn

                    // 輸入框區域
                    MessageInput(
                        onSendMessage = { text ->
                            // --- 調用 ViewModel 的方法 ---
                            viewModel.sendMessage(text)
                        },
                        // Pass Modifier if needed for imePadding/navigationBarsPadding inside MessageInput
                        // modifier = Modifier...
                    )
                }
            } // end when
        } // end Column
    } // end Scaffold content
}

// --- Preview needs adjustment as it now requires ViewModel ---
@Preview(showBackground = true)
@Composable
fun ChatDetailScreenPreview() {
    SignalAppTheme {
        // Previewing screens with ViewModels using `viewModel()` is tricky.
        // It will likely show the initial loading state.
        // Better to create a stateful preview or preview the content part directly.
        Text("ChatDetailScreen Preview (ViewModel injected)")
        // Or simulate state for preview
        // ChatDetailScreenContent( // Assuming a separate content composable exists
        //     uiState = ChatDetailUiState(isLoading = false, participantName = "Preview User", messages = ...),
        //     onSendMessage = {},
        //     onNavigateBack = {}
        // )
    }
}
