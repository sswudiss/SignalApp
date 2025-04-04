package com.example.signalapp.screens.chat

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.signalapp.ui.theme.JJLLTheme

// 假設的聊天數據模型
data class ChatItem(val id: String, val name: String, val lastMessage: String, val timestamp: String)

// 模擬的聊天列表數據
val sampleChatList = listOf(
    ChatItem("user1", "Alice", "好的，明天見！", "10:30"),
    ChatItem("user2", "Bob", "你收到文件了嗎？", "昨天"),
    ChatItem("group1", "Project Team", "會議紀要已發送", "星期一"),
    ChatItem("self_user_id", "給自己的消息", "備忘錄...", "16:45"), // 模擬自己的聊天
)

@Composable
fun ChatListScreen(
    onNavigateToChat: (String) -> Unit // 接收聊天 ID
) {
    // 實際應用中，這裡會從 ViewModel 獲取聊天列表數據
    val chatList = remember { sampleChatList }

    if (chatList.isEmpty()) {
        // 如果沒有聊天記錄，顯示提示信息
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("暫無聊天記錄", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        // 使用 LazyColumn 顯示可滾動的聊天列表
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(chatList, key = { it.id }) { chatItem ->
                ChatItemRow(
                    chatItem = chatItem,
                    onClick = { onNavigateToChat(chatItem.id) } // 點擊時導航到詳情頁
                )
                Divider() // 添加分割線
            }
        }
    }

    // // 臨時添加按鈕測試導航到自己
    // Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
    //     Button(onClick = { onNavigateToChat("self_user_id") }) {
    //         Text("測試導航到自己")
    //     }
    // }
}

// 單個聊天列表項的 Composable
@Composable
fun ChatItemRow(
    chatItem: ChatItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick) // 使整行可點擊
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // TODO: 添加頭像 (Avatar)
        // Placeholder for Avatar
        Box(modifier = Modifier.size(40.dp).padding(end = 16.dp)) {
            Text(chatItem.name.first().toString()) // 暫時用名字首字母代替頭像
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(text = chatItem.name, style = MaterialTheme.typography.titleMedium)
            Text(text = chatItem.lastMessage, style = MaterialTheme.typography.bodyMedium, maxLines = 1)
        }
        Text(text = chatItem.timestamp, style = MaterialTheme.typography.bodySmall)
    }
}


@RequiresApi(Build.VERSION_CODES.S)
@Preview(showBackground = true)
@Composable
fun ChatListScreenPreview() {
    JJLLTheme {
        ChatListScreen(onNavigateToChat = {})
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Preview(showBackground = true)
@Composable
fun ChatItemRowPreview() {
    JJLLTheme {
        ChatItemRow(chatItem = sampleChatList[0], onClick = {})
    }
}