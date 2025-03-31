package com.example.signalapp

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.signalapp.data.Conversation // 導入數據模型
import com.example.signalapp.ui.theme.SignalAppTheme

// 聊天列表畫面 (暫時只顯示文字)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListScreen(navController: NavHostController) { // 接收 NavController
    // --- 假數據 (之後會替換成真實數據) ---
    val conversations = remember { // 使用 remember 避免每次重組都創建新列表
        listOf(
            Conversation("user123", "Alice", "好的，待會見"),
            Conversation("user456", "Bob", "哈哈，明白了！"),
            Conversation("group789", "Compose 學習小組", "今晚有分享嗎？")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("訊息") }, // Signal 風格的標題
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
                // 可以添加 Actions (例如搜索按鈕)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // 應用 padding
        ) {
            items(conversations, key = { it.id }) { conversation ->
                ConversationListItem(
                    conversation = conversation,
                    onClick = {
                        // --- 觸發導航 ---
                        // 構建目標路由，將實際的 conversation id 填入
                        val route = "${AppDestinations.CHAT_ROUTE}/${conversation.id}"
                        println("Navigating to: $route") // 打印日誌方便調試
                        navController.navigate(route)
                    }
                )
                Divider(modifier = Modifier.padding(horizontal = 16.dp)) // 分隔線樣式
            }
        }
    }
}

@Composable
fun ConversationListItem(conversation: Conversation, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick) // 使整行可點擊
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 可以在這裡加一個頭像的 Placeholder
        // Icon(painter = painterResource(id = R.drawable.ic_person), contentDescription = "頭像")
        // Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(conversation.name, style = MaterialTheme.typography.titleMedium) // 加粗一點的標題
            Text(
                conversation.lastMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, // 柔和的顏色
                maxLines = 1 // 限制只顯示一行
            )
        }
        // 可以在這裡加時間戳
        // Text("10:30", style = MaterialTheme.typography.bodySmall)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewConversationListScreen() {
    SignalAppTheme {
        // 為了預覽，傳入一個假的 NavController
        ConversationListScreen(navController = rememberNavController())
    }
}