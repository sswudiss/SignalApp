package com.example.signalapp.ui.chatdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp


// --- 消息項目的 Composable (ChatMessageItem) ---
// 這個應該在你之前的步驟中已經定義好了
@Composable
fun ChatMessageItem(message: ChatMessage) {
    // ... (之前的實現，包括根據 isSentByCurrentUser 調整對齊和背景色)
    val horizontalAlignment = if (message.isSentByCurrentUser) Alignment.End else Alignment.Start
    val backgroundColor = if (message.isSentByCurrentUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (message.isSentByCurrentUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
    val bubbleShape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (message.isSentByCurrentUser) 16.dp else 0.dp,
        bottomEnd = if (message.isSentByCurrentUser) 0.dp else 16.dp
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = horizontalAlignment
    ) {
        Box(
            modifier = Modifier
                .wrapContentWidth()
                .clip(bubbleShape)
                .background(backgroundColor)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(text = message.text, color = textColor, style = MaterialTheme.typography.bodyLarge)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = formatMessageTimestamp(message.timestamp), // 需要 formatMessageTimestamp 函數
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}
