package com.example.signalapp.data.mapper

import com.example.signalapp.data.local.entity.ChatMessageEntity
import com.example.signalapp.data.local.entity.ChatSummaryEntity
import com.example.signalapp.ui.chatdetail.ChatMessage
import com.example.signalapp.ui.chatlist.ChatSummary


// 將 ChatSummaryEntity 轉換為 ChatSummary (用於 UI)
fun ChatSummaryEntity.toChatSummary(): ChatSummary {
    return ChatSummary(
        id = this.chatId,
        participantName = this.participantName,
        lastMessage = this.lastMessagePreview,
        timestamp = this.lastMessageTimestamp,
        unreadCount = this.unreadCount
        // participantAvatarUrl = this.participantAvatarUrl // 如果有頭像
    )
}

// 將 List<ChatSummaryEntity> 轉換為 List<ChatSummary>
fun List<ChatSummaryEntity>.toChatSummaryList(): List<ChatSummary> {
    return this.map { it.toChatSummary() }
}

// 將 ChatMessageEntity 轉換為 ChatMessage (用於 UI)
fun ChatMessageEntity.toChatMessage(): ChatMessage {
    return ChatMessage(
        id = this.messageId,
        text = this.text,
        timestamp = this.timestamp,
        senderId = this.senderId,
        isSentByCurrentUser = this.isSentByCurrentUser
    )
}

// 將 List<ChatMessageEntity> 轉換為 List<ChatMessage>
fun List<ChatMessageEntity>.toChatMessageList(): List<ChatMessage> {
    return this.map { it.toChatMessage() }
}

// --- 可能還需要反向的轉換 (從 UI 模型到 Entity)，用於保存數據 ---
// fun ChatMessage.toEntity(chatId: String): ChatMessageEntity { ... }
// fun ChatSummary.toEntity(): ChatSummaryEntity { ... }