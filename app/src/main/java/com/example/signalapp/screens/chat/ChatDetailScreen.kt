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
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.signalapp.model.Message
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


// 主 Composable - 使用 Hilt 獲取 ViewModel
@Composable
fun ChatDetailScreen(
    contactId: String, // contactId 現在代表 conversationId
    navController: NavController,
    // 使用 Hilt 自動獲取 ViewModel 實例
    viewModel: ChatDetailViewModel = hiltViewModel() // <--- 使用 hiltViewModel()
) {
    // 將 UI 邏輯委託給 Content Composable
    ChatDetailScreenContent(
        contactId = contactId,
        navController = navController,
        viewModel = viewModel // 傳遞由 Hilt 提供的 ViewModel
    )
}


// UI 內容 Composable - 接收 ViewModel 作為參數 (方便預覽注入假 ViewModel)
@Composable
private fun ChatDetailScreenContent( // 設為 private 或 internal
    contactId: String,
    navController: NavController,
    viewModel: ChatDetailViewModel // 直接接收 ViewModel
) {
    // --- State (從傳入的 viewModel 獲取) ---
    val inputText = viewModel.inputText
    val messages by viewModel.messages.collectAsState()
    val contactName = viewModel.contactName

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    // --- Effects ---
    // 當 contactId (conversationId) 變化時，設置給 ViewModel
    LaunchedEffect(contactId, viewModel) { // 添加 viewModel 作為 key，確保實例變化時也觸發
        viewModel.setConversationId(contactId)
    }

    // 當有新消息時，自動滾動到底部
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            val lastMessage = messages.last()
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
            val shouldScroll = lastMessage.senderId == currentUserId ||
                    (listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1) >= messages.size - 2

            if (shouldScroll) {
                coroutineScope.launch {
                    delay(100)
                    listState.animateScrollToItem(messages.size - 1)
                }
            }
        }
    }


    // --- UI ---
    Scaffold(
        topBar = {
            ChatDetailTopAppBar(
                contactName = contactName,
                onBackClick = { navController.popBackStack() },
                onVideoClick = { viewModel.onVideoCallClick() },
                viewModel = viewModel // 仍然需要將 viewModel 傳遞給子組件
            )
        },
        bottomBar = {
            ChatInputArea(
                inputText = inputText,
                onInputChange = viewModel::onInputChange,
                onSendClick = viewModel::sendMessage,
                onAddClick = viewModel::onAddAttachment,
                onCameraClick = viewModel::onCameraClick,
                onVoiceClick = viewModel::onVoiceClick,
                focusRequester = focusRequester
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) { // 鍵盤外部點擊隱藏鍵盤
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // 應用 Scaffold 的 Padding
        ) {
            // 可以根據需要顯示加載狀態
            // if (viewModel.isLoading) { CircularProgressIndicator(modifier = Modifier.align(Alignment.Center)) }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp) // 消息間的垂直間距
            ) {
                // 使用從 StateFlow 收集到的 messages 列表
                items(messages, key = { it.id }) { message ->
                    // 判斷是否是當前用戶需要 currentUser Id
                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                    MessageBubble(
                        message = message,
                        // 使用真實用戶 ID 判斷，注意 currentUserId 可能為 null (雖然理論上進入此頁面應該已登錄)
                        isCurrentUser = message.senderId == currentUserId
                    )
                }
            }
        }
    }
}


// --- 子組件 (保持不變) ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailTopAppBar(
    contactName: String,
    onBackClick: () -> Unit,
    onVideoClick: () -> Unit,
    viewModel: ChatDetailViewModel // 接收 ViewModel 來處理菜單項點擊
) {
    var showMenu by rememberSaveable { mutableStateOf(false) } // 使用 rememberSaveable 保存菜單狀態

    TopAppBar(
        title = { Text(contactName, maxLines = 1) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
            }
        },
        actions = {
            IconButton(onClick = onVideoClick) {
                Icon(Icons.Filled.Videocam, contentDescription = "視頻通話")
            }
            IconButton(onClick = { showMenu = !showMenu }) { // 點擊切換菜單顯示
                Icon(Icons.Filled.MoreVert, contentDescription = "菜單")
            }
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
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@SuppressLint("SimpleDateFormat")
@Composable
fun MessageBubble(message: Message, isCurrentUser: Boolean) {
    val alignment = if (isCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    val backgroundColor = if (isCurrentUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
    val textColor = if (isCurrentUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
    val bubbleShape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isCurrentUser) 16.dp else 0.dp,
        bottomEnd = if (isCurrentUser) 0.dp else 16.dp
    )
    val formatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val timeString = remember(message.timestamp) { formatter.format(Date(message.timestamp)) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .align(alignment)
                .widthIn(max = LocalConfiguration.current.screenWidthDp.dp * 0.75f)
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
                color = textColor.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.End)
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
    onCameraClick: () -> Unit, // 參數保留
    onVoiceClick: () -> Unit,  // 參數保留
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
            verticalAlignment = Alignment.Bottom
        ) {
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
                        if (!isTextEmpty) {
                            onSendClick()
                        }
                    }
                ),
                maxLines = 5
            )

            if (isTextEmpty) {
                IconButton(onClick = onAddClick) {
                    Icon(Icons.Filled.Add, contentDescription = "添加附件", modifier = Modifier.size(28.dp))
                }
            } else {
                IconButton(
                    onClick = onSendClick,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(8.dp)
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