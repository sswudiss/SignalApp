package com.example.signalapp.screens.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.signalapp.model.Contact
import com.example.signalapp.ui.theme.JJLLTheme

/*
* ViewModel: 使用 hiltViewModel() 獲取 ContactsViewModel。
狀態收集: 使用 viewModel.contacts.collectAsState() 觀察聯繫人列表。
顯示列表: 使用 LazyColumn 和 items 遍歷 contacts 狀態來顯示列表，調用 ContactItemRow。
測試按鈕 (FAB): 添加了一個 FloatingActionButton (FAB)。
* 你需要將代碼中的 REPLACE_WITH_KNOWN_USER_ID 和 REPLACE_WITH_KNOWN_USERNAME 替換成你用來測試的、
* 已註冊的另一個賬號的真實信息！ 點擊這個按鈕會調用 viewModel.addContactManually 將測試聯繫人寫入 Room。
空列表提示: 當 contacts 為空時顯示提示文本。
點擊跳轉: ContactItemRow 的 onClick 現在調用 onNavigateToChat(contact.userId)，傳遞正確的用戶 ID 給聊天頁面。

ContactItemRow 修改:
參數改為接收 Contact 對象。
顯示邏輯改為優先顯示 displayName。
增加了刪除按鈕及其回調 onDeleteClick。
預覽: 更新了預覽以顯示包含/不包含數據的列表，以及單個 ContactItemRow 的樣子。
* */

@Composable
fun ContactsScreen(
    viewModel: ContactsViewModel = hiltViewModel(), // 使用 Hilt 獲取 ViewModel
    onNavigateToChat: (String) -> Unit // 接收聯繫人 ID (User ID)
) {
    // 從 ViewModel 收集聯繫人列表狀態
    val contacts by viewModel.contacts.collectAsState()

    Scaffold( // 可以加 Scaffold 提供統一結構，或者直接用 Column
        // TODO: 添加搜索欄等 UI (階段二)
        floatingActionButton = { // 臨時添加一個按鈕用於手動添加聯繫人測試
            FloatingActionButton(onClick = {
                // 假設我們知道要添加的用戶 ID 和用戶名 (需要先註冊好)
                val testUserId = "REPLACE_WITH_KNOWN_USER_ID" // <<< 替換成一個你註冊的、非當前用戶的 ID
                val testUsername = "REPLACE_WITH_KNOWN_USERNAME" // <<< 替換成對應的用戶名
                if (testUserId.startsWith("REPLACE")) {
                    // 提醒用戶替換
                    println("請在 ContactsScreen.kt 中替換 testUserId 和 testUsername")
                } else {
                    viewModel.addContactManually(testUserId, testUsername, testUsername) // 添加測試用戶
                    println("Attempting to add contact: ID=$testUserId, Username=$testUsername")
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "手動添加測試聯繫人")
            }
        }
    ) { paddingValues ->

        if (contacts.isEmpty()) {
            // 如果沒有聯繫人，顯示提示信息
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("通訊錄是空的", style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "點擊右下角按鈕添加一個測試用戶",
                        style = MaterialTheme.typography.bodySmall
                    ) // 測試提示
                    Text(
                        "(請修改代碼中的用戶ID和用戶名)",
                        style = MaterialTheme.typography.bodySmall
                    ) // 測試提示
                }
            }
        } else {
            // 使用 LazyColumn 顯示聯繫人列表
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 80.dp) // 避免被 FAB 遮擋
            ) {
                // 可以按首字母分組 (需要更複雜邏輯)
                // stickyHeader { /* Group Header */ }
                items(contacts, key = { it.userId }) { contact ->
                    ContactItemRow( // 使用之前的 ContactItemRow
                        contact = contact, // 傳遞真實的 Contact 對象
                        onClick = { onNavigateToChat(contact.userId) }, // 點擊時傳遞 User ID
                        onDeleteClick = { viewModel.deleteContact(contact) } // 添加刪除回調
                    )
                    Divider(modifier = Modifier.padding(start = 72.dp)) // 分割線
                }
            }
        }
    }
}

// 修改 ContactItemRow 以接收 Contact 對象，並添加刪除按鈕
@Composable
fun ContactItemRow(
    contact: Contact, // 接收 Contact 對象
    onClick: () -> Unit,
    onDeleteClick: () -> Unit // 新增刪除回調
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick) // 使整行可點擊
            .padding(horizontal = 16.dp, vertical = 8.dp), // 減小垂直 padding
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 聯繫人頭像 (可以用 Coil 加載 contact.photoUrl，目前暫用圖標)
        Box( // 用 Box 包裹頭像方便添加 Badge 等
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer), // 增大頭像尺寸，加背景
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person, // 用 Person 圖標
                contentDescription = "聯繫人頭像",
                modifier = Modifier.size(32.dp), // 調整圖標大小
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            // TODO: 使用 Coil 加載 contact.photoUrl
            // AsyncImage( model = contact.photoUrl, ... )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // 聯繫人名稱 (優先顯示 displayName)
        Column(modifier = Modifier.weight(1f)) { // 用 Column 顯示多行信息
            Text(
                text = contact.displayName ?: contact.username, // 優先顯示名，否則顯示用戶名
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1 // 限制單行
            )
            Text(
                text = "@${contact.username}", // 同時顯示用戶名 (如果需要)
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                maxLines = 1
            )
        }


        // 添加刪除按鈕
        IconButton(onClick = onDeleteClick) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "刪除聯繫人",
                tint = MaterialTheme.colorScheme.error // 紅色提示
            )
        }
    }
}

// 預覽部分需要調整以適應 ViewModel 或傳遞假數據
@Preview(showBackground = true)
@Composable
fun ContactsScreenEmptyPreview() {
    JJLLTheme {
        // 預覽需要提供假的 ViewModel 或直接控制狀態
        // 為了簡單，這裡直接顯示空狀態的 UI 佈局
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("通訊錄是空的", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ContactsScreenWithDataPreview() {
    JJLLTheme {
        // 創建假數據用於預覽列表項
        val previewContacts = listOf(
            Contact("user1", "alice_u", "Alice Wonderland", null),
            Contact("user2", "bob_k", "Bob The Builder", null)
        )
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(previewContacts, key = { it.userId }) { contact ->
                ContactItemRow(
                    contact = contact,
                    onClick = {},
                    onDeleteClick = {} // 預覽中不需要真實刪除
                )
                Divider(modifier = Modifier.padding(start = 72.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ContactItemRowPreview() {
    JJLLTheme {
        ContactItemRow(
            contact = Contact("user1", "alice_u", "Alice W.", "url..."),
            onClick = {},
            onDeleteClick = {}
        )
    }
}