package com.example.signalapp.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert // 使用 Upsert (更新或插入) 很方便
import com.example.signalapp.data.local.entity.ChatMessageEntity
import com.example.signalapp.data.local.entity.ChatSummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    /**
     * 獲取所有聊天摘要，按最後消息時間降序排列。
     * 返回 Flow，可以在數據變化時自動更新 UI。
     */
    @Query("SELECT * FROM chat_summaries ORDER BY lastMessageTimestamp DESC")
    fun getAllChatSummaries(): Flow<List<ChatSummaryEntity>>

    /**
     * 根據 chatId 獲取特定聊天的所有消息，按時間升序排列 (舊->新)。
     * 返回 Flow。
     */
    @Query("SELECT * FROM chat_messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChat(chatId: String): Flow<List<ChatMessageEntity>>

    /**
     * 插入或更新一個聊天摘要。
     * 如果 chatId 已存在，則更新記錄；否則，插入新記錄。
     */
    @Upsert // Upsert = Insert or Update
    suspend fun upsertChatSummary(summary: ChatSummaryEntity)

    /**
     * 插入一條新的聊天消息。
     * 注意：通常插入新消息後，你需要手動更新對應的 ChatSummary。
     */
    @Upsert // 也可以用 Upsert，如果 messageId 可能重複（雖然不應該）
    suspend fun insertMessage(message: ChatMessageEntity)

    /**
     * (可選，用於更新摘要) 根據 chatId 查找最新的消息時間戳。
     */
    @Query("SELECT MAX(timestamp) FROM chat_messages WHERE chatId = :chatId")
    suspend fun getLatestMessageTimestamp(chatId: String): Long?

    // --- 其他可能的方法 ---
    // @Query("UPDATE chat_summaries SET unreadCount = 0 WHERE chatId = :chatId")
    // suspend fun markChatAsRead(chatId: String)
    //
    // @Query("DELETE FROM chat_summaries WHERE chatId = :chatId")
    // suspend fun deleteChat(chatId: String) // Cascade 會自動刪除消息

    /**
     * 根據 chatId 獲取聊天摘要 (如果需要單獨獲取)。
     */
    @Query("SELECT * FROM chat_summaries WHERE chatId = :chatId LIMIT 1")
    suspend fun getChatSummaryById(chatId: String): ChatSummaryEntity?

}