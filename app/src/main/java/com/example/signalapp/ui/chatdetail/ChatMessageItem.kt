package com.example.signalapp.ui.chatdetail

// ... (其他導入)
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.signalapp.ui.theme.SignalAppTheme
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatMessageItem(message: ChatMessage) {
    // 根據是否由當前用戶發送來決定對齊方式和背景色
    val horizontalAlignment = if (message.isSentByCurrentUser) Alignment.End else Alignment.Start
    val backgroundColor =
        if (message.isSentByCurrentUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val textColor =
        if (message.isSentByCurrentUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
    // 設定不同的圓角
    val bubbleShape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (message.isSentByCurrentUser) 16.dp else 0.dp,
        bottomEnd = if (message.isSentByCurrentUser) 0.dp else 16.dp
    )

    // 使用 Box 來容納消息氣泡和下方可能的時間戳
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp), // 消息之間的間距
        horizontalAlignment = horizontalAlignment // 控制 Column 內元素是靠左還是靠右
    ) {
        // 消息氣泡
        Box(
            modifier = Modifier
                .wrapContentWidth() // 氣泡寬度由內容決定
                .clip(bubbleShape)
                .background(backgroundColor)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = message.text,
                color = textColor,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        // 可以選擇在這裡或氣泡內顯示時間戳
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = formatMessageTimestamp(message.timestamp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp) // 給時間戳一點邊距
        )
    }
}


// 格式化消息時間戳
private fun formatMessageTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    // 可以做得更複雜，比如加上日期
    val pattern = "HH:mm"
    return SimpleDateFormat(pattern, Locale.getDefault()).format(date)
}

// 消息項預覽
@Preview(showBackground = true, name = "Sent Message")
@Composable
fun SentMessagePreview() {
    val msg =
        ChatMessage("1", "你好，這是發送的消息！", System.currentTimeMillis(), "currentUser", true)
    SignalAppTheme {
        ChatMessageItem(message = msg)
    }
}

@Preview(showBackground = true, name = "Received Message")
@Composable
fun ReceivedMessagePreview() {
    val msg = ChatMessage(
        "2",
        "收到了，這是來自對方的回覆。",
        System.currentTimeMillis() - 10000,
        "otherUser",
        false
    )
    SignalAppTheme {
        ChatMessageItem(message = msg)
    }
}