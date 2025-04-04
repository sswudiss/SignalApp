package com.example.signalapp.model

import java.util.UUID // 用於生成唯一 ID
import androidx.room.Entity
import androidx.room.PrimaryKey

// 使用 @Entity 標註，並定義表名
@Entity(tableName = "messages")
data class Message(
    // 使用 @PrimaryKey 標註主鍵，autoGenerate = true 讓 Room 自動生成遞增 ID
    // 如果你想繼續用 UUID，也可以設置為 @PrimaryKey val id: String = UUID.randomUUID().toString()
    // 但自增 Long ID 通常更高效
    // @PrimaryKey(autoGenerate = true) val localId: Long = 0, // 使用數據庫本地 ID 作為主鍵

    // 保留你的 UUID 作為消息的唯一標識符，但它不是數據庫主鍵
    // 或者直接用你的 UUID 作為主鍵，如下所示：
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val text: String,
    val timestamp: Long = System.currentTimeMillis(), // 發送時間戳
    val senderId: String, // 發送者 ID (例如 "me", "user1", "user2")

    // 添加一個字段來標識這條消息屬於哪個對話 (非常重要!)
    // 這個 ID 可以是對方的 userId (對於1對1聊天) 或一個 groupID (對於群聊)
    // 或者是你自己的 userID (對於 "給自己的消息")
    val conversationId: String,

    // 可以添加更多字段，如 isSent, isRead, type (text/image), localPath 等
    // val isSent: Boolean = false,
    // val isRead: Boolean = false,
)