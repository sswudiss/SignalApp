package com.example.signalapp.model

import java.util.UUID // 用於生成唯一 ID

data class Message(
    val id: String = UUID.randomUUID().toString(), // 唯一標識符
    val text: String,
    val timestamp: Long = System.currentTimeMillis(), // 發送時間戳
    val senderId: String, // 發送者 ID ("me" 或 contactId)
    // 可以添加更多字段，如 isRead, messageType (text, image, etc.)
)