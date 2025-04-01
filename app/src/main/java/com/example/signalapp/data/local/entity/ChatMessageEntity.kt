package com.example.signalapp.data.local.entity


import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_messages",
    // 添加外鍵，確保 message 的 chatId 必須對應一個存在的 chat_summary
    // 如果 chat_summary 被刪除，對應的消息也應被處理 (例如級聯刪除 onDelete = ForeignKey.CASCADE)
    foreignKeys = [ForeignKey(
        entity = ChatSummaryEntity::class,
        parentColumns = ["chatId"],
        childColumns = ["chatId"],
        onDelete = ForeignKey.CASCADE // 當對應的聊天摘要被刪除時，刪除此消息
    )],
    // 為 chatId 添加索引，加快按聊天查詢消息的速度
    indices = [Index("chatId")]
)
data class ChatMessageEntity(
    @PrimaryKey
    val messageId: String, // 每條消息的唯一 ID (可以用 UUID 生成)

    val chatId: String, // 關聯到哪個聊天

    val text: String, // 消息內容
    val timestamp: Long, // 發送時間戳
    val senderId: String, // 發送者 ID ("currentUser" 或對方 ID)
    val isSentByCurrentUser: Boolean, // 是否由當前用戶發送
    // 可以添加狀態字段，如是否已發送、已送達、已讀 (isSent, isDelivered, isRead)
    // val status: Int = 0
)