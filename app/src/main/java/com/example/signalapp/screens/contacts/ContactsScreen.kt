package com.example.signalapp.screens.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.*
import androidx.compose.material.icons.filled.* // 導入更多圖標
import androidx.compose.ui.text.input.ImeAction // For keyboard action
import androidx.compose.foundation.text.KeyboardActions // For keyboard action
import androidx.compose.foundation.text.KeyboardOptions // For keyboard action
import androidx.compose.material.icons.Icons
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.signalapp.model.FoundUser
import com.example.signalapp.model.Contact
import com.example.signalapp.ui.theme.JJLLTheme


/**
 * 通訊錄屏幕 Composable，包含搜索欄、搜索結果和本地聯繫人列表。
 *
 * @param viewModel ContactsViewModel 實例，由 Hilt 自動注入。
 * @param onNavigateToChat 導航到聊天詳情頁的回調，傳遞對方用戶的 ID。
 */
@Composable
fun ContactsScreen(
    viewModel: ContactsViewModel = hiltViewModel(), // 使用 Hilt 獲取 ViewModel
    onNavigateToChat: (String) -> Unit // 接收聯繫人 ID (User ID)
) {
    // --- 從 ViewModel 收集狀態 ---
    val contacts by viewModel.contacts.collectAsState() // 本地聯繫人列表
    val searchQuery = viewModel.searchQuery           // 當前搜索查詢
    val searchResults = viewModel.searchResults      // 搜索結果列表
    val isSearching = viewModel.isSearching           // 是否正在搜索的標誌
    val searchErrorMessage = viewModel.searchErrorMessage // 搜索錯誤信息

    // 使用 remember 計算已添加聯繫人的 ID 集合，避免每次重組都計算
    val contactUserIds = remember(contacts) { contacts.map { it.userId }.toSet() }

    Scaffold(
        topBar = { // 將搜索欄放置在頂部應用欄
            SearchBar(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChange, // 綁定查詢變化事件
                onClearQuery = viewModel::clearSearch, // 綁定清空查詢事件
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp) // 設置邊距
            )
        }
        // 不再需要手動添加的 FAB
        // floatingActionButton = { ... }
    ) { paddingValues -> // Scaffold 提供的內邊距

        // 主內容區域，使用 Column 排列搜索結果和聯繫人列表
        Column(
            modifier = Modifier
                .fillMaxSize()              // 佔滿可用空間
                .padding(paddingValues)     // 應用 Scaffold 提供的內邊距
        ) {

            // --- 搜索結果顯示區域 ---
            // 只有在正在搜索、有結果或有錯誤時才顯示這個區域
            if (isSearching || searchResults.isNotEmpty() || searchErrorMessage != null) {
                SearchContent(
                    results = searchResults,
                    isSearching = isSearching,
                    errorMessage = searchErrorMessage,
                    onAddContact = viewModel::addContactFromSearch, // 添加按鈕的回調
                    isAlreadyContact = { userId -> userId in contactUserIds } // 判斷是否已添加的回調
                )
                Divider() // 在搜索結果和聯繫人列表之間添加分隔線
            }

            // --- 本地聯繫人列表顯示區域 ---
            // 只有在沒有進行搜索且聯繫人列表為空時，顯示“空列表”提示
            if (contacts.isEmpty() && searchQuery.isBlank() && !isSearching) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f), // 佔滿剩餘空間
                    contentAlignment = Alignment.Center // 居中顯示提示文本
                ) {
                    Text(
                        "通訊錄是空的，請使用上方搜索添加聯繫人",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                // 如果聯繫人列表不為空，則顯示列表
            } else if (contacts.isNotEmpty()){
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f), // 佔滿剩餘空間
                    contentPadding = PaddingValues(bottom = 16.dp) // 底部留出邊距
                ) {
                    item { // 添加一個標題項
                        Text(
                            "我的聯繫人",
                            style = MaterialTheme.typography.titleSmall, // 使用小標題樣式
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp) // 標題邊距
                        )
                    }
                    // 遍歷聯繫人列表，為每個聯繫人創建一個 ContactItemRow
                    items(contacts, key = { contact -> contact.userId }) { contact ->
                        ContactItemRow(
                            contact = contact,
                            onClick = { onNavigateToChat(contact.userId) }, // 列表項點擊導航
                            onDeleteClick = { viewModel.deleteContact(contact) } // 刪除按鈕點擊事件
                        )
                        HorizontalDivider(modifier = Modifier.padding(start = 72.dp)) // 在頭像後開始的分隔線
                    }
                }
                // 如果正在顯示搜索結果 (即使聯繫人列表為空)，或者聯繫人列表原本就為空但正在搜索
            } else if (searchQuery.isNotBlank() || isSearching){
                // 這種情況下不需要顯示 "列表為空" 的提示，底部留白即可
                Spacer(modifier = Modifier.weight(1f)) // 使用 Spacer 佔據剩餘空間
            }
            // 注意: 如果搜索查詢為空，也不在搜索，聯繫人也為空，則由第一個 if 處理。
        }
    }
}


// --- 子組件 ---

/**
 * 搜索欄 Composable。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,                      // 綁定搜索查詢文本
        onValueChange = onQueryChange,       // 文本變化時的回調
        modifier = modifier.fillMaxWidth(), // 佔滿寬度
        placeholder = { Text("按用戶名搜索用戶...") }, // 提示文字
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "搜索圖標") }, // 前導搜索圖標
        trailingIcon = { // 尾部圖標（清空按鈕）
            if (query.isNotEmpty()) { // 只有當輸入框非空時才顯示清空按鈕
                IconButton(onClick = onClearQuery) {
                    Icon(Icons.Default.Close, contentDescription = "清空搜索框")
                }
            }
        },
        shape = CircleShape, // 設置為圓角（膠囊形狀）
        colors = OutlinedTextFieldDefaults.colors( // 自定義顏色
            // 設置背景色
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            // 設置邊框為透明，使其看起來更像一個填充的搜索欄
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        ),
        singleLine = true,                   // 限制為單行輸入
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search), // 設置鍵盤動作為“搜索”
        keyboardActions = KeyboardActions(onSearch = { /* 通常在 onChange 時已處理搜索邏輯 */ })
    )
}

/**
 * 顯示搜索結果區域的 Composable。
 */
@Composable
fun SearchContent(
    results: List<FoundUser>,         // 搜索結果列表
    isSearching: Boolean,            // 是否正在搜索
    errorMessage: String?,          // 搜索錯誤信息
    onAddContact: (FoundUser) -> Unit,// 添加聯繫人的回調
    isAlreadyContact: (String) -> Boolean // 檢查用戶 ID 是否已在聯繫人中的回調
) {
    // 使用 Column 垂直排列結果
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        if (isSearching) {
            // 如果正在搜索，顯示加載指示器
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 16.dp))
        } else if (errorMessage != null) {
            // 如果有錯誤信息，顯示錯誤文本
            Text(
                errorMessage,
                color = MaterialTheme.colorScheme.error, // 使用錯誤顏色
                modifier = Modifier.padding(vertical = 16.dp).align(Alignment.CenterHorizontally)
            )
        } else if (results.isNotEmpty()) {
            // 如果有搜索結果，顯示結果列表
            Text(
                "搜索結果:", // 列表標題
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top=8.dp, bottom = 4.dp)
            )
            // 注意：如果搜索結果可能非常多，這裡也應該使用 LazyColumn
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) { // 使用 Column 排列少量結果
                results.forEach { user ->
                    SearchResultItem(
                        user = user,
                        onAddClick = { onAddContact(user) }, // 添加按鈕點擊事件
                        isAdded = isAlreadyContact(user.userId) // 傳遞是否已添加的狀態
                    )
                    Divider() // 每個結果項後加分隔線
                }
            }
            Spacer(modifier = Modifier.height(8.dp)) // 結果列表底部增加間距
        }
        // 如果以上條件都不滿足（不在搜索、無錯誤、無結果），則此 Composable 不顯示任何內容
    }
}

/**
 * 顯示單個搜索結果項的 Composable。
 */
@Composable
fun SearchResultItem(
    user: FoundUser,       // 要顯示的用戶數據
    onAddClick: () -> Unit, // 添加按鈕點擊事件回調
    isAdded: Boolean       // 該用戶是否已經是本地聯繫人
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), // 設置垂直內邊距
        verticalAlignment = Alignment.CenterVertically // 垂直居中對齊
    ) {
        // 頭像顯示區域
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), // 頭像大小和背景
            contentAlignment = Alignment.Center
        ) {
            // TODO: 使用 Coil 加載 user.photoUrl
            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
        }
        Spacer(modifier = Modifier.width(12.dp)) // 頭像和文字間距

        // 用戶名和顯示名
        Column(modifier = Modifier.weight(1f)) { // 佔據主要空間
            Text(
                user.displayName ?: user.username, // 優先顯示名
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "@${user.username}", // 顯示用戶名
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
        Spacer(modifier = Modifier.width(8.dp)) // 文字和按鈕間距

        // 根據 isAdded 狀態顯示 "已添加" 文本或 "添加" 按鈕
        if (isAdded) {
            Text(
                "已添加",
                style = MaterialTheme.typography.labelMedium, // 使用標籤樣式
                color = MaterialTheme.colorScheme.primary    // 使用主題主色調
            )
        } else {
            Button(
                onClick = onAddClick,                           // 按鈕點擊事件
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp), // 減小按鈕內邊距
                modifier = Modifier.height(36.dp)                 // 限制按鈕高度
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加聯繫人圖標", modifier = Modifier.size(18.dp)) // 添加圖標
                Spacer(Modifier.width(4.dp)) // 圖標和文字間距
                Text("添加")
            }
        }
    }
}

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