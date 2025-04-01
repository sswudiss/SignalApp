package com.example.signalapp.ui.chatlist


data class ChatListUiState(
    val chats: List<ChatSummary> = emptyList(), // 聊天列表
    val isLoading: Boolean = true,            // 初始狀態為正在加載
    val errorMessage: String? = null          // 錯誤訊息
)