package com.example.signalapp.data.repository


import com.example.signalapp.data.local.dao.ChatDao
import com.example.signalapp.data.local.entity.ChatMessageEntity
import com.example.signalapp.data.local.entity.ChatSummaryEntity
import com.example.signalapp.data.mapper.toChatMessageList
import com.example.signalapp.data.mapper.toChatSummaryList
import com.example.signalapp.ui.chatdetail.ChatMessage // UI 模型
import com.example.signalapp.ui.chatlist.ChatSummary   // UI 模型
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map // 導入 flow 的 map 操作符
import kotlinx.coroutines.withContext
import java.util.UUID // 用於生成 message ID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val chatDao: ChatDao
    // private val userDao: UserDao // 如果需要用戶信息可以注入
) {

    /**
     * 獲取所有聊天摘要的 Flow (已經映射為 UI 模型)。
     */
    fun getChatSummaries(): Flow<List<ChatSummary>> {
        return chatDao.getAllChatSummaries()
            .map { entityList -> entityList.toChatSummaryList() } // 使用 map 操作符轉換 Flow 中的列表
    }

    /**
     * 獲取特定聊天的消息 Flow (已經映射為 UI 模型)。
     */
    fun getMessagesForChat(chatId: String): Flow<List<ChatMessage>> {
        return chatDao.getMessagesForChat(chatId)
            .map { entityList -> entityList.toChatMessageList() }
    }

    /**
     * 保存一條新消息，並更新對應的聊天摘要。
     * @param chatId 聊天 ID
     * @param text 消息內容
     * @param senderId 發送者 ID
     * @param isSentByCurrentUser 是否是當前用戶發送
     */
    suspend fun saveNewMessage(
        chatId: String,
        text: String,
        senderId: String,
        isSentByCurrentUser: Boolean
    ) {
        withContext(Dispatchers.IO) { // 在 IO 線程執行數據庫操作
            val timestamp = System.currentTimeMillis()
            val messageId = UUID.randomUUID().toString() // 生成唯一消息 ID

            val newMessage = ChatMessageEntity(
                messageId = messageId,
                chatId = chatId,
                text = text,
                timestamp = timestamp,
                senderId = senderId,
                isSentByCurrentUser = isSentByCurrentUser
            )

            // 1. 插入新消息
            chatDao.insertMessage(newMessage)

            // 2. 更新聊天摘要
            //  a. 獲取現有摘要 (如果不存在，可能需要創建一個)
            var summary = chatDao.getChatSummaryById(chatId)

            // 如果摘要不存在，創建一個新的 (需要知道參與者名字，這裡模擬一下)
            if (summary == null) {
                val partnerName = getSimulatedPartnerName(chatId) // 模擬獲取對方名字
                summary = ChatSummaryEntity(
                    chatId = chatId,
                    participantName = partnerName,
                    lastMessagePreview = text,
                    lastMessageTimestamp = timestamp,
                    // 新會話的第一條消息，如果是對方發的，未讀數+1
                    unreadCount = if (isSentByCurrentUser) 0 else 1
                )
            } else {
                // 更新現有摘要
                summary = summary.copy(
                    lastMessagePreview = text, // 更新預覽
                    lastMessageTimestamp = timestamp, // 更新時間戳
                    // 如果是接收到的消息，未讀數增加
                    unreadCount = if (isSentByCurrentUser) summary.unreadCount else summary.unreadCount + 1
                )
            }

            // b. 將更新後的摘要寫回數據庫
            chatDao.upsertChatSummary(summary)

            // 注意：這裡的兩步操作 (插入消息 + 更新摘要) 不是原子性的。
            // 更好的做法是將它們放在一個 Room 事務 (@Transaction) 中確保一致性。
            // 但為了簡化，我們先這樣做。
        }
    }

    // 模擬獲取對方名字（應從 UserDao 或其他來源獲取）
    private fun getSimulatedPartnerName(chatId: String): String {
        // 根據 chatId 返回假名字，或者默認值
        return "用戶 $chatId"
    }


    // --- 將來可能需要的方法 ---
    // suspend fun createNewChat(participants: List<String>) { ... }
    // suspend fun markChatRead(chatId: String) { ... }
}