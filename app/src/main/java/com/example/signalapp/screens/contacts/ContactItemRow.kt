package com.example.signalapp.screens.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person // 導入 Person 圖標
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview // 導入 Preview
import androidx.compose.ui.unit.dp
import com.example.signalapp.model.Contact
import com.example.signalapp.ui.theme.JJLLTheme

/**
 * 用於顯示單個聯繫人信息的行 Composable。
 * @param contact 要顯示的 Contact 對象。
 * @param onClick 整行被點擊時的回調。
 * @param onDeleteClick 刪除按鈕被點擊時的回調。
 */
@Composable
fun ContactItemRow(
    contact: Contact, // 接收 Contact 對象
    onClick: () -> Unit,
    onDeleteClick: () -> Unit // 新增刪除回調
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()             // 佔滿父容器寬度
            .clickable(onClick = onClick) // 使整行都可以響應點擊事件
            .padding(horizontal = 16.dp, vertical = 8.dp), // 設置水平和垂直內邊距
        verticalAlignment = Alignment.CenterVertically // 垂直居中對齊行內元素
    ) {
        // 頭像顯示區域 (目前使用圖標作為佔位符)
        Box( // 使用 Box 包裹頭像，方便未來添加角標等
            modifier = Modifier
                .size(48.dp)                  // 設置頭像區域大小
                .clip(CircleShape)            // 裁剪成圓形
                .background(MaterialTheme.colorScheme.primaryContainer), // 設置背景色
            contentAlignment = Alignment.Center // 內容（圖標）居中顯示
        ) {
            // TODO: 後續使用 Coil 等庫異步加載 contact.photoUrl 顯示真實頭像
            // AsyncImage( model = contact.photoUrl, contentDescription = "聯繫人頭像", ...)
            Icon(
                imageVector = Icons.Default.Person,   // 使用默認的 Person 圖標
                contentDescription = "聯繫人頭像",       // 無障礙描述
                modifier = Modifier.size(32.dp),      // 調整圖標大小
                tint = MaterialTheme.colorScheme.onPrimaryContainer // 設置圖標顏色
            )
        }

        Spacer(modifier = Modifier.width(16.dp)) // 頭像和文字之間的水平間距

        // 聯繫人名稱顯示區域 (包含顯示名和用戶名)
        Column(modifier = Modifier.weight(1f)) { // 使用 Column 垂直排列，並佔據剩餘的主要空間
            // 主要顯示的名稱 (優先使用 displayName)
            Text(
                text = contact.displayName ?: contact.username, // 如果 displayName 為 null，則顯示 username
                style = MaterialTheme.typography.bodyLarge,    // 使用較大的正文字體
                maxLines = 1                                   // 限制只顯示一行，超出部分會被截斷
            )
            // 次要顯示的用戶名 (帶 @ 前綴)
            Text(
                text = "@${contact.username}",                      // 添加 @ 符號前綴
                style = MaterialTheme.typography.bodySmall,        // 使用較小的正文字體
                color = MaterialTheme.colorScheme.outline,        // 使用較淺的輪廓顏色
                maxLines = 1                                       // 同樣限制只顯示一行
            )
        }

        //Spacer(modifier = Modifier.width(8.dp)) // 可以加一個小的間距，如果需要的話

        // 刪除按鈕區域
        IconButton(onClick = onDeleteClick) { // 圖標按鈕，點擊觸發刪除回調
            Icon(
                imageVector = Icons.Filled.Delete,      // 使用刪除圖標
                contentDescription = "刪除聯繫人",       // 無障礙描述
                tint = MaterialTheme.colorScheme.error // 使用錯誤顏色（通常是紅色）來突出顯示
            )
        }
    }
}


// --- 預覽 Composable ---
@Preview(showBackground = true)
@Composable
fun ContactItemRowPreview() {
    JJLLTheme { // 應用你的 App 主題
        ContactItemRow(
            contact = Contact( // 提供一個模擬的 Contact 對象用於預覽
                userId = "preview_user_id",
                username = "preview_username",
                displayName = "預覽顯示名",
                photoUrl = null // 預覽時沒有頭像 URL
            ),
            onClick = {}, // 預覽時點擊事件為空實現
            onDeleteClick = {} // 預覽時刪除事件為空實現
        )
    }
}