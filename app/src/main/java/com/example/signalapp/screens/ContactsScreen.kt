package com.example.signalapp.screens


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.signalapp.ui.theme.JJLLTheme

// 假設的聯繫人數據模型
data class ContactItem(val id: String, val name: String)

// 模擬的聯繫人列表數據
val sampleContactList = listOf(
    ContactItem("user1", "Alice"),
    ContactItem("user2", "Bob"),
    ContactItem("user3", "Charlie"),
    ContactItem("user4", "David"),
    ContactItem("user5", "Eve"),
    ContactItem("user6", "Frank"),
)

@Composable
fun ContactsScreen(
    onNavigateToChat: (String) -> Unit // 接收聯繫人 ID
) {
    // 實際應用中，這裡會從 ViewModel 或數據庫獲取聯繫人列表
    val contactList = remember { sampleContactList }

    if (contactList.isEmpty()) {
        // 如果沒有聯繫人，顯示提示信息
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("暫無聯繫人", style = MaterialTheme.typography.bodyLarge)
        }
    } else {
        // 使用 LazyColumn 顯示可滾動的聯繫人列表
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            // 可以添加分組標題等，這裡僅簡單列出
            items(contactList, key = { it.id }) { contact ->
                ContactItemRow(
                    contact = contact,
                    onClick = { onNavigateToChat(contact.id) } // 點擊時導航到與該聯繫人的聊天
                )
                Divider(modifier = Modifier.padding(start = 72.dp)) // 分割線，左側留出頭像空間
            }
        }
    }
}

// 單個聯繫人列表項的 Composable
@Composable
fun ContactItemRow(
    contact: ContactItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick) // 使整行可點擊
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 聯繫人頭像 (暫用圖標)
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "聯繫人頭像",
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.primary // 設置圖標顏色
        )

        Spacer(modifier = Modifier.width(16.dp))

        // 聯繫人名稱
        Text(
            text = contact.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f) // 佔滿剩餘空間
        )
        // 可以添加其他按鈕，如電話圖標等（如果需要）
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Preview(showBackground = true)
@Composable
fun ContactsScreenPreview() {
    JJLLTheme {
        ContactsScreen(onNavigateToChat = {})
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Preview(showBackground = true)
@Composable
fun ContactItemRowPreview() {
    JJLLTheme {
        ContactItemRow(contact = ContactItem("user1", "Alice"), onClick = {})
    }
}