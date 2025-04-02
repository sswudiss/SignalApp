package com.example.signalapp.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.* // 導入 Material 3 組件
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage // 使用 Coil 加載圖片 (如果需要)
import coil.request.ImageRequest
import com.example.signalapp.data.ChatPreview
import com.example.signalapp.data.DummyDataProvider
import com.example.signalapp.ui.theme.SignalAppTheme
import com.example.signalapp.R


@Composable
fun ChatListScreen() {
    val chats = DummyDataProvider.chatList // 獲取假數據

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp) // 列表上下留白
    ) {
        items(chats, key = { chat -> chat.id }) { chat ->
            ChatItem(chat = chat, onClick = {
                // TODO: 處理點擊事件，導航到聊天詳情頁
                println("Clicked on chat: ${chat.contactName}")
            })
            // Divider() // 可以取消註釋添加分隔線
        }
    }
    // TODO: 可以在 Scaffold 中添加 FAB 用於創建新聊天
}

@OptIn(ExperimentalMaterial3Api::class) // ListItem 需要此 OptIn
@Composable
fun ChatItem(
    chat: ChatPreview,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = {
            Text(
                text = chat.contactName,
                fontWeight = FontWeight.SemiBold, // 名字加粗
                fontSize = 16.sp
            )
        },
        supportingContent = {
            Text(
                text = chat.lastMessage,
                maxLines = 1, // 限制單行
                overflow = TextOverflow.Ellipsis, // 超出部分顯示省略號
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant // 使用次要文本顏色
            )
        },
        leadingContent = {
            // 頭像 - 使用 Coil 加載網絡圖片或顯示默認圖標
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(chat.avatarUrl)
                    .crossfade(true) // 淡入效果
                    .placeholder(R.drawable.ic_launcher_foreground) // 佔位符 (你需要添加一個) - 或者使用 Icon
                    .error(R.drawable.ic_launcher_foreground)       // 錯誤佔位符 - 或者使用 Icon
                    .build(),
                contentDescription = "${chat.contactName} avatar",
                contentScale = ContentScale.Crop, // 裁剪方式
                modifier = Modifier
                    .size(40.dp) // 頭像大小
                    .clip(CircleShape) // 裁剪為圓形
            )
            // 如果不用 Coil 或沒有圖片 URL，可以使用默認圖標:
//            Icon(
//                imageVector = Icons.Default.AccountCircle,
//                contentDescription = "${chat.contactName} avatar",
//                modifier = Modifier.size(40.dp),
//                tint = MaterialTheme.colorScheme.primary // 給圖標上色
//            )
        },
        trailingContent = {
            Text(
                text = chat.timestamp,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        // colors = ListItemDefaults.colors(...) // 可以自定義顏色
        tonalElevation = 1.dp // 給一點點陰影/層次感
    )
    // 你也可以不使用 ListItem，完全自定義 Row + Column 佈局
}

@Preview(showBackground = true)
@Composable
fun ChatListScreenPreview() {
    SignalAppTheme {
        ChatListScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun ChatItemPreview() {
    SignalAppTheme {
        ChatItem(chat = DummyDataProvider.chatList[0], onClick = {})
    }
}