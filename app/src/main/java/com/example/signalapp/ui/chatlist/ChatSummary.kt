package com.example.signalapp.ui.chatlist

/*
* 表示聊天列表需要顯示的資訊
* */
data class ChatSummary(
    val id: String, // 唯一標識符
    val participantName: String,
    val lastMessage: String?, // 可能沒有最後一條消息
    val timestamp: Long, // 用 Long 存儲時間戳，便於排序
    val unreadCount: Int = 0, // 未讀消息數
    val participantAvatarUrl: String? = null // 可選的頭像 URL
)