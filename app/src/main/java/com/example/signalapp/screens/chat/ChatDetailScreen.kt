package com.example.signalapp.screens.chat

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack // 使用 AutoMirrored 圖標適應 RTL
import androidx.compose.material.icons.automirrored.filled.Send // 使用 AutoMirrored 圖標
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.signalapp.model.Message
import com.example.signalapp.ui.theme.JJLLTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    contactId: String,
    navController: NavController,
    viewModel: ChatDetailViewModel = viewModel() // 獲取 ViewModel 實例
) {
    // --- State ---
    val inputText = viewModel.inputText
    val messages = viewModel.messages
    val contactName = viewModel.contactName
    val isLoading = viewModel.isLoading
    val listState = rememberLazyListState() // 用於滾動列表
    val coroutineScope = rememberCoroutineScope() // 用於啟動協程滾動列表
    val focusManager = LocalFocusManager.current // 用於管理鍵盤焦點
    val focusRequester = remember { FocusRequester() } // 控制 TextField 焦點

    // --- Effects ---
    // 當 contactId 變化時加載消息
    LaunchedEffect(contactId) {
        viewModel.loadMessages(contactId)
    }

    // 當有新消息時，自動滾動到底部
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            coroutineScope.launch {
                // 延遲一小會兒確保佈局完成
                delay(100)
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    // --- UI ---
    Scaffold(
        // 這個 Screen 有自己的 TopAppBar
        topBar = {
            ChatDetailTopAppBar(
                contactName = contactName,
                onBackClick = { navController.popBackStack() }, // 返回上一頁
                onVideoClick = { viewModel.onVideoCallClick() },
                onMenuClick = { /* TODO: Show Dropdown Menu */ }, // 佔位，下面會添加下拉菜單
                viewModel = viewModel // 將 ViewModel 傳遞給 TopAppBar 以便處理菜單項
            )
        },
        // 輸入區域放在 bottomBar slot，這樣它總在底部，鍵盤會把它推上去
        bottomBar = {
            ChatInputArea(
                inputText = inputText,
                onInputChange = viewModel::onInputChange, // 使用方法引用
                onSendClick = {
                    viewModel.sendMessage()
                    // focusManager.clearFocus() // 發送後可以選擇是否清除焦點
                },
                onAddClick = viewModel::onAddAttachment,
                onCameraClick = viewModel::onCameraClick,
                onVoiceClick = viewModel::onVoiceClick,
                focusRequester = focusRequester
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .imePadding() // 添加 imePadding 來處理鍵盤遮擋
            // 添加 pointerInput 來檢測主內容區域的點擊，以隱藏鍵盤
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
    ) { paddingValues -> // Scaffold 提供的內邊距

        // --- 消息列表 ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // 應用 Scaffold 內邊距

        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    // reverseLayout = true // 反轉佈局，最新的消息在底部，但輸入新消息會讓它跳，更好的方式是正常佈局+滾動到底部
                    verticalArrangement = Arrangement.spacedBy(8.dp) // 消息間的垂直間距
                ) {
                    items(messages, key = { it.id }) { message ->
                        MessageBubble(
                            message = message,
                            isCurrentUser = message.senderId == "me" // 判斷是否是當前用戶發送
                        )
                    }
                }
            }
        }
    }
}


// --- 子組件 ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailTopAppBar(
    contactName: String,
    onBackClick: () -> Unit,
    onVideoClick: () -> Unit,
    onMenuClick: () -> Unit, // 替換為直接觸發下拉菜單顯示的邏輯
    viewModel: ChatDetailViewModel // 接收 ViewModel 來處理菜單項點擊
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(contactName, maxLines = 1) }, // 顯示聯繫人姓名
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
        },
        actions = {
            IconButton(onClick = onVideoClick) {
                Icon(Icons.Filled.Videocam, contentDescription = "視頻通話")
            }
            // 菜單按鈕
            IconButton(onClick = { showMenu = !showMenu }) { // 點擊切換菜單顯示
                Icon(Icons.Filled.MoreVert, contentDescription = "菜單")
            }
            // 下拉菜單
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false } // 點擊菜單外部關閉
            ) {
                DropdownMenuItem(
                    text = { Text("清除聊天記錄") },
                    onClick = {
                        viewModel.onMenuClearHistory()
                        showMenu = false // 關閉菜單
                    }
                )
                DropdownMenuItem(
                    text = { Text("自動銷毀消息") },
                    onClick = {
                        viewModel.onMenuDisappearingMessages()
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("字體大小") },
                    onClick = {
                        viewModel.onMenuFontSize()
                        showMenu = false
                    }
                )
            }
        },
        // 固定在頂部，設置顏色等
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}




@SuppressLint("SimpleDateFormat") // 處理日期格式化警告
@Composable
fun MessageBubble(message: Message, isCurrentUser: Boolean) {
    val alignment = if (isCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    val backgroundColor =
        if (isCurrentUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val textColor =
        if (isCurrentUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
    val bubbleShape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isCurrentUser) 16.dp else 0.dp, // 根據發送者調整尖角
        bottomEnd = if (isCurrentUser) 0.dp else 16.dp
    )
    // 簡單的時間格式化
    val formatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeString = remember(message.timestamp) { formatter.format(Date(message.timestamp)) }


    Box(modifier = Modifier.fillMaxWidth()) { // 讓氣泡可以在 Box 內左右對齊
        Column(
            modifier = Modifier
                .align(alignment) // 將 Column 對齊到 Box 的 Start 或 End
                .widthIn(max = LocalConfiguration.current.screenWidthDp.dp * 0.7f) // 限制最大寬度
                .clip(bubbleShape)
                .background(backgroundColor)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = message.text,
                color = textColor,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = timeString,
                color = textColor.copy(alpha = 0.7f), // 時間文字稍暗
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.End) // 時間戳靠右
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInputArea(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onAddClick: () -> Unit,
    onCameraClick: () -> Unit, // 需要這個回調
    onVoiceClick: () -> Unit,  // 需要這個回調
    focusRequester: FocusRequester
) {
    val isTextEmpty = inputText.isBlank()
    val focusManager = LocalFocusManager.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 輸入框
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChange,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
                    .focusRequester(focusRequester),
                placeholder = { Text("輸入消息...") },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        onSendClick()
                        // focusManager.clearFocus()
                    }
                ),
                maxLines = 5,

                // ******** 新增/修改的部分 ********
                trailingIcon = {
                    // 只有當輸入框為空時，才顯示內部圖標
                    if (isTextEmpty) {
                        Row { // 將相機和語音圖標放在一行
                            IconButton(onClick = onCameraClick, modifier = Modifier.size(48.dp)) { // 給予足夠的點擊區域
                                Icon(
                                    Icons.Filled.PhotoCamera,
                                    contentDescription = "拍照",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant // 可以自定義顏色
                                )
                            }
                            // 可以加個小的間隔
                            // Spacer(modifier = Modifier.width(4.dp))
                            IconButton(onClick = onVoiceClick, modifier = Modifier.size(48.dp)) {
                                Icon(
                                    Icons.Filled.Mic,
                                    contentDescription = "錄音",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    // 如果 inputText 不為空，trailingIcon lambda 返回 Unit，即不顯示任何東西
                }
                // ******** 新增/修改結束 ********
            )

            // ****** 外部按鈕區域 ******
            // 這個 Row 只包含 加號 或 發送 按鈕
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isTextEmpty) {
                    // 輸入框為空時，顯示加號按鈕
                    IconButton(onClick = onAddClick) {
                        Icon(Icons.Filled.Add, contentDescription = "添加附件", modifier = Modifier.size(28.dp))
                    }
                    // **** 確保這裡沒有重複的相機和語音圖標 ****
                } else {
                    // 輸入框有內容時，顯示發送按鈕
                    IconButton(
                        onClick = onSendClick,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "發送",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

// --- Preview ---
// 在 Preview 中更新 ChatInputArea 的調用，確保傳入了 onCameraClick 和 onVoiceClick

@Preview(showBackground = true)
@Composable
fun ChatInputAreaEmptyPreview() {
    JJLLTheme {
        ChatInputArea(
            inputText = "",
            onInputChange = {},
            onSendClick = {},
            onAddClick = {},
            onCameraClick = {}, // 添加回調
            onVoiceClick = {},  // 添加回調
            focusRequester = FocusRequester()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ChatInputAreaWithTextPreview() {
    JJLLTheme {
        ChatInputArea(
            inputText = "Hello there!",
            onInputChange = {},
            onSendClick = {},
            onAddClick = {},
            onCameraClick = {}, // 添加回調
            onVoiceClick = {},  // 添加回調
            focusRequester = FocusRequester()
        )
    }
}
// --- Preview ---
@Preview(showBackground = true)
@Composable
fun ChatDetailScreenPreview() {
    JJLLTheme { // 或 JJLLTheme
        ChatDetailScreen(
            contactId = "user_preview",
            navController = rememberNavController()
            // ViewModel 會自動創建模擬數據
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MessageBubbleCurrentUserPreview() {
    JJLLTheme {
        MessageBubble(
            message = Message(
                text = "這是當前用戶發送的一條比較長的消息，看看換行效果。",
                senderId = "me"
            ), isCurrentUser = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MessageBubbleOtherUserPreview() {
    JJLLTheme {
        MessageBubble(
            message = Message(text = "這是對方用戶發送的消息。", senderId = "other"),
            isCurrentUser = false
        )
    }
}