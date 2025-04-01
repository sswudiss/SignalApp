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

@Composable
fun MessageInput(
    onSendMessage: (String) -> Unit, // 回調函數，傳遞要發送的文本
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val isSendEnabled = inputText.isNotBlank() // 只有在有輸入時才啟用發送按鈕

    // Surface 包裹以提供背景色和陰影 (可選)
    Surface(
        modifier = modifier.fillMaxWidth(), // 橫向填滿
        shadowElevation = 4.dp // 給一點陰影
        // color = MaterialTheme.colorScheme.surface // 可自定義背景色
    ) {
        // 使用 Row 來水平排列輸入框和按鈕
        Row(
            modifier = Modifier
                .padding(8.dp) // 添加內邊距
                .navigationBarsPadding(), // 為底部導航欄添加 padding (如果有的話)
//                .imePadding(), // <--- 關鍵：為軟鍵盤彈出添加 padding，將輸入區域向上推
            verticalAlignment = Alignment.CenterVertically // 垂直居中
        ) {
            // 文本輸入框
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("輸入消息...") },
                modifier = Modifier
                    .weight(1f) // 佔據大部分寬度
                    .padding(end = 8.dp), // 和按鈕之間留點距離
                // 可以自定義樣式，比如移除邊框，改變形狀等
                shape = RoundedCornerShape(24.dp),
//                 colors = TextFieldDefaults.outlinedTextFieldColors(...)
                maxLines = 4 // 限制最大行數，防止無限增高
            )

            // 發送按鈕 (只有在 inputText 非空時才啟用)
            IconButton(
                onClick = {
                    if (isSendEnabled) {
                        onSendMessage(inputText) // 觸發回調
                        inputText = "" // 清空輸入框
                    }
                },
                enabled = isSendEnabled, // 根據輸入內容啟用/禁用
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary, // 圖標顏色
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.disabled) // 禁用時的顏色
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "發送消息"
                )
            }
        }
    }
}

// 輸入區域預覽
@Preview(showBackground = true)
@Composable
fun MessageInputPreview() {
    SignalAppTheme {
        MessageInput(onSendMessage = {})
    }
}