package com.example.signalapp.screens.contacts

import androidx.compose.foundation.background
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
import com.example.signalapp.model.FoundUser


@Composable
fun ContactsScreen(
    viewModel: ContactsViewModel = hiltViewModel(),
    onNavigateToChat: (String) -> Unit
) {
    val contacts by viewModel.contacts.collectAsState()
    val searchQuery = viewModel.searchQuery
    val searchResults = viewModel.searchResults
    val isSearching = viewModel.isSearching
    val searchErrorMessage = viewModel.searchErrorMessage

    // 用於判斷某個搜索結果是否已是聯繫人
    val contactUserIds = remember(contacts) { contacts.map { it.userId }.toSet() }

    Scaffold(
        topBar = { // 將搜索欄放在 TopAppBar
            SearchBar(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChange, // 調用 ViewModel 的方法
                onClearQuery = viewModel::clearSearch, // 添加清空按鈕回調
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        // --- 移除手動添加的 FAB ---
        // floatingActionButton = { ... }
    ) { paddingValues ->

        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // --- 搜索結果區域 (只有在搜索或有結果/錯誤時顯示) ---
            if (isSearching || searchResults.isNotEmpty() || searchErrorMessage != null) {
                SearchContent(
                    results = searchResults,
                    isSearching = isSearching,
                    errorMessage = searchErrorMessage,
                    onAddContact = viewModel::addContactFromSearch, // 添加聯繫人回調
                    isAlreadyContact = { userId -> userId in contactUserIds } // 判斷是否已是聯繫人
                )
                HorizontalDivider() // 分隔搜索結果和聯繫人列表
            }

            // --- 固定顯示本地聯繫人列表 ---
            if (contacts.isEmpty() && searchQuery.isBlank() && !isSearching) { // 只有在未搜索且無本地聯繫人時顯示空提示
                Box(
                    modifier = Modifier.fillMaxSize().weight(1f), // 佔滿剩餘空間
                    contentAlignment = Alignment.Center
                ) {
                    Text("通訊錄是空的，請使用上方搜索添加聯繫人", style = MaterialTheme.typography.bodyLarge)
                }
            } else if (contacts.isNotEmpty()){ // 有聯繫人時才顯示列表
                LazyColumn(
                    modifier = Modifier.fillMaxSize().weight(1f), // 佔滿剩餘空間
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    item { // 添加一個列表標題
                        Text(
                            "我的聯繫人",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                    items(contacts, key = { it.userId }) { contact ->
                        ContactItemRow(
                            contact = contact,
                            onClick = { onNavigateToChat(contact.userId) },
                            onDeleteClick = { viewModel.deleteContact(contact) }
                        )
                        Divider(modifier = Modifier.padding(start = 72.dp))
                    }
                }
            } else if (searchQuery.isBlank() && !isSearching) {
                // 如果聯繫人列表為空，但在顯示搜索結果時，底部留白
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}


// 搜索欄 Composable
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("按用戶名搜索用戶...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "搜索") },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClearQuery) {
                    Icon(Icons.Default.Close, contentDescription = "清空")
                }
            }
        },
        shape = CircleShape, // 圓角保持不變
        colors = OutlinedTextFieldDefaults.colors( // *** 修改這裡 ***
            // 使用 M3 的新參數：
            focusedTextColor = MaterialTheme.colorScheme.onSurface, // 焦點時文本顏色
            unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant, // 非焦點時文本顏色 (可以和上面一樣)
            cursorColor = MaterialTheme.colorScheme.primary, // 光標顏色
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), // 焦點時容器背景色
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), // 非焦點時容器背景色
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), // 禁用時容器背景色

            // 邊框顏色控制參數 (對於 OutlinedTextField):
            focusedBorderColor = Color.Transparent, // 焦點時邊框透明
            unfocusedBorderColor = Color.Transparent, // 非焦點時邊框透明
            // 如果想顯示邊框，可以設置為：
            // focusedBorderColor = MaterialTheme.colorScheme.primary,
            // unfocusedBorderColor = MaterialTheme.colorScheme.outline,

            // placeholder 和 Icon 顏色等通常會繼承或有默認值，也可以單獨設置
            // focusedPlaceholderColor = ...,
            // unfocusedPlaceholderColor = ...,
            // focusedLeadingIconColor = ...,
        ),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { /* 通常不需要做什麼，因為 onChange 已觸發 */ })
    )
}

// 顯示搜索結果的 Composable
@Composable
fun SearchContent(
    results: List<FoundUser>,
    isSearching: Boolean,
    errorMessage: String?,
    onAddContact: (FoundUser) -> Unit,
    isAlreadyContact: (String) -> Boolean // 函數：檢查某用戶是否已是聯繫人
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        if (isSearching) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 16.dp))
        } else if (errorMessage != null) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 16.dp))
        } else if (results.isNotEmpty()) {
            Text("搜索結果:", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top=8.dp, bottom = 4.dp))
            // 如果結果多，這裡也應該用 LazyColumn
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) { // 使用 Column 顯示少量結果
                results.forEach { user ->
                    SearchResultItem(
                        user = user,
                        onAddClick = { onAddContact(user) },
                        isAdded = isAlreadyContact(user.userId) // 傳遞是否已添加的狀態
                    )
                    Divider()
                }
            }
            Spacer(modifier = Modifier.height(8.dp)) // 結果底部間距
        }
        // else - 沒有結果，也沒有錯誤，也不在搜索中 -> 不顯示任何東西
    }
}

// 單個搜索結果項
@Composable
fun SearchResultItem(
    user: FoundUser,
    onAddClick: () -> Unit,
    isAdded: Boolean // 是否已添加為聯繫人
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 頭像 (同 ContactItemRow 邏輯)
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
            // TODO: Coil 加載 user.photoUrl
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(user.displayName ?: user.username, style = MaterialTheme.typography.bodyMedium)
            Text("@${user.username}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
        // 添加按鈕 (如果未添加) 或提示 (如果已添加)
        if (isAdded) {
            Text("已添加", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        } else {
            Button(onClick = onAddClick, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp), modifier = Modifier.height(36.dp)) {
                Icon(Icons.Default.Add, contentDescription = "添加", modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("添加")
            }
        }
    }
}