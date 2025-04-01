package com.example.signalapp.ui.newchat // <--- 包名已更新

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // 導入 items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person // 佔位圖標
import androidx.compose.material3.*
import androidx.compose.runtime.* // 導入 collectAsState, LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel // 導入 Hilt ViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.signalapp.navigation.AppDestinations
import com.example.signalapp.ui.theme.SignalAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewChatScreen(
    navController: NavController, // 接收 NavController 用於導航
    viewModel: NewChatViewModel = hiltViewModel() // 使用 Hilt 獲取 ViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    // 監聽 navigateToChatId 狀態變化以觸發導航
    LaunchedEffect(uiState.navigateToChatId) {
        uiState.navigateToChatId?.let { chatId ->
            // 導航到聊天詳情頁
            navController.navigate(AppDestinations.chatDetailPath(chatId)) {
                // 可以選擇是否從後退棧彈出 NewChatScreen
                // popUpTo(AppDestinations.NEW_CHAT_ROUTE) { inclusive = true }
            }
            // 導航後通知 ViewModel 重置狀態
            viewModel.navigationDone()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("選擇聯繫人") }, // 更改標題
                navigationIcon = {
                    // 返回按鈕使用 NavController 的 navigateUp
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null -> {
                    Text(
                        "錯誤: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                uiState.contacts.isEmpty() -> {
                    Text(
                        "沒有其他已註冊用戶",
                        modifier = Modifier.align(Alignment.Center).padding(16.dp)
                    )
                }
                else -> {
                    // 使用 LazyColumn 顯示聯繫人列表
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(
                            items = uiState.contacts,
                            key = { contact -> contact.username } // 用 username 作為 key
                        ) { contact ->
                            ContactItem(
                                contact = contact,
                                onClick = {
                                    viewModel.onContactSelected(contact.username)
                                }
                            )
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

// 用於顯示單個聯繫人的 Composable
@Composable
fun ContactItem(
    contact: ContactUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick) //  جعل الصف بأكمله قابل للنقر
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Person, // 佔位符圖標
            contentDescription = "聯繫人頭像",
            modifier = Modifier.size(40.dp) // 比聊天列表頭像小一點
                .padding(end = 16.dp)
            // TODO: 將來加載真實頭像
        )
        Text(
            text = contact.displayName,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}


@Preview(showBackground = true)
@Composable
fun NewChatScreenPreview() {
    SignalAppTheme {
        // 預覽需要提供假的 NavController
        val dummyNavController = rememberNavController() // rememberNavController 在 Preview 中可能受限
        Box(Modifier.fillMaxSize()) { Text("NewChatScreen Preview Requires Interaction") }
        // NewChatScreen(navController = dummyNavController) // 可能無法正常預覽
    }
}

@Preview(showBackground = true)
@Composable
fun ContactItemPreview() {
    SignalAppTheme {
        ContactItem(ContactUiModel("johndoe", "John Doe"), onClick = {})
    }
}