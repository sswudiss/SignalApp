package com.example.signalapp.ui.chatdetail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send // Send icon
import androidx.compose.material3.*
import androidx.compose.runtime.* // for remember, mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.imePadding // <--- 需要這個處理鍵盤
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ContentAlpha
import androidx.compose.ui.tooling.preview.Preview
import com.example.signalapp.ui.theme.SignalAppTheme

// --- 輸入框的 Composable (MessageInput) ---
// 這個也應該在你之前的步驟中定義好了，注意參數可能需要微調
@Composable
fun MessageInput(
    currentText: String, // 使用 ViewModel 的狀態
    onInputChange: (String) -> Unit, // 回調 ViewModel 更新
    onSendMessage: () -> Unit, // 回調 ViewModel 發送
    modifier: Modifier = Modifier
) {
    val isSendEnabled = currentText.isNotBlank() // 發送按鈕是否啟用

    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 4.dp // 可選陰影
    ) {
        Row(
            modifier = Modifier
                // 只保留頂部和水平 padding，底部交給 imePadding
                .padding(horizontal = 8.dp).padding(top = 8.dp)
                .navigationBarsPadding() // 處理導航欄遮擋
                .imePadding(), // *** 關鍵：處理鍵盤彈出 ***
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = currentText, // 綁定外部狀態
                onValueChange = onInputChange, // 調用外部回調
                placeholder = { Text("輸入消息...") },
                modifier = Modifier
                    .weight(1f) // 佔滿剩餘空間
                    .padding(end = 8.dp), // 與按鈕間距
                maxLines = 4 // 限制最大輸入行數
                // 可以自定義 shape, colors 等
            )
            IconButton(
                onClick = onSendMessage, // 調用外部發送回調
                enabled = isSendEnabled,
                // ... (colors 等)
            ) {
                Icon(Icons.Filled.Send, contentDescription = "發送")
            }
        }
    }
}

// --- 格式化時間戳的輔助函數 ---
// 這個應該也定義過
fun formatMessageTimestamp(timestamp: Long): String {
    // ... (之前的實現)
    val date = java.util.Date(timestamp)
    val pattern = "HH:mm"
    return java.text.SimpleDateFormat(pattern, java.util.Locale.getDefault()).format(date)
}
