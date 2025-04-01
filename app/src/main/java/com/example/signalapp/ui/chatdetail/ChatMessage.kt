package com.example.signalapp.ui.chatdetail


data class ChatMessage(
    val id: String,             // 消息的唯一 ID
    val text: String,           // 消息內容
    val timestamp: Long,        // 發送時間戳
    val senderId: String,       // 發送者 ID (可以是 "currentUser" 或對方的 ID)
    val isSentByCurrentUser: Boolean // 標記是否由當前用戶發送
)