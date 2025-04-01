package com.example.signalapp.data.local.entity


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_summaries")
data class ChatSummaryEntity(
    @PrimaryKey
    val chatId: String, // 唯一的聊天 ID (例如可以是對方的用戶名，或群組ID)

    val participantName: String, // 對方名稱 (實際應用可能需要關聯 User 表)
    val lastMessagePreview: String?, // 最後一條消息的預覽 (可空)
    val lastMessageTimestamp: Long, // 最後一條消息的時間戳 (用於排序)
    val unreadCount: Int = 0, // 未讀消息數
    // 你可以添加其他字段，如是否置頂 (isPinned) 等
    // val participantAvatarUrl: String? = null // 如果需要存頭像鏈接
)