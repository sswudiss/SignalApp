package com.example.signalapp.ui.chatlist

import androidx.compose.foundation.Image // For avatar (using placeholder for now)
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable // Make item clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape // For circular avatar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person // Placeholder avatar
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.signalapp.ui.theme.SignalAppTheme
import java.text.SimpleDateFormat // For formatting timestamp
import java.util.Date
import java.util.Locale

// --- 新增的 ChatItem Composable ---
@Composable
fun ChatItem(
    chatSummary: ChatSummary,
    onClick: (String) -> Unit, // 傳遞點擊事件，參數為 chat ID
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(chatSummary.id) } // 整個 Row 可點擊
            .padding(horizontal = 16.dp, vertical = 12.dp), // 增加垂直內邊距
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 頭像區域 (Placeholder)
        Image(
            imageVector = Icons.Default.Person, // 使用預設圖標作為佔位符
            contentDescription = "${chatSummary.participantName} 頭像",
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape) // 裁切成圓形
                .background(MaterialTheme.colorScheme.primaryContainer) // 給個背景色
        )
        // TODO: 將來可以使用 Coil 或 Glide 加載 participantAvatarUrl

        Spacer(modifier = Modifier.width(16.dp))

        // 姓名、最後訊息、時間戳 區域 (垂直排列)
        Column(
            modifier = Modifier
                .weight(1f) // 佔據剩餘的可用寬度
        ) {
            Text(
                text = chatSummary.participantName,
                style = MaterialTheme.typography.titleMedium, // 加粗一些
                maxLines = 1,
                overflow = TextOverflow.Ellipsis // 過長時顯示省略號
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = chatSummary.lastMessage ?: "", // 如果為 null 顯示空字串
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, // 使用次要顏色
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp)) // 與右側內容的間距

        // 時間戳和未讀計數區域 (垂直排列，靠右對齊)
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = formatTimestamp(chatSummary.timestamp), // 格式化時間戳
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (chatSummary.unreadCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                // 未讀計數徽章
                Badge(
                    containerColor = MaterialTheme.colorScheme.primary // 使用主題強調色
                ) {
                    Text(
                        text = chatSummary.unreadCount.toString(),
                        modifier = Modifier.padding(horizontal = 4.dp) // 給徽章一點橫向 padding
                        // color = MaterialTheme.colorScheme.onPrimary // Badge 會自動處理內部顏色
                    )
                }
                // 如果想自定義更複雜的徽章，可以不用 Badge，自己用 Box + Text 實現
                /*Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(20.dp) // 固定大小
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = chatSummary.unreadCount.toString(),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }*/
            }
        }
    }
}

// 簡單的時間戳格式化輔助函數
private fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    // 可以根據時間差顯示 "昨天", "星期几" 等，這裡先用簡單格式
    val pattern = if (android.text.format.DateUtils.isToday(timestamp)) {
        "HH:mm" // 今天顯示時間
    } else {
        "MM/dd" // 否則顯示月/日
    }
    return SimpleDateFormat(pattern, Locale.getDefault()).format(date)
}

// --- ChatItem 的預覽 (創建一個假數據實例) ---
@Preview(showBackground = true)
@Composable
fun ChatItemPreview() {
    val previewChat = ChatSummary("prev", "預覽用戶", "這是一條預覽消息...", System.currentTimeMillis(), 3)
    SignalAppTheme {
        ChatItem(chatSummary = previewChat, onClick = {})
    }
}