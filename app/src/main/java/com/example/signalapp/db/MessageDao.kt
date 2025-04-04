package com.example.signalapp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.signalapp.model.Message
import kotlinx.coroutines.flow.Flow // 導入 Flow

@Dao
interface MessageDao {

    /**
     * 插入一條消息。如果已存在相同 id 的消息，則替換它。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message) // suspend 關鍵字表示這是掛起函數，需要在協程中調用

    /**
     * 根據對話 ID 獲取所有相關消息，並按時間戳升序排序。
     * 返回 Flow<List<Message>>，這樣當數據庫變化時，觀察者會自動收到更新。
     */
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<Message>>

    /**
     * 根據對話 ID 刪除所有相關消息。
     */
    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: String)

    /**
     * (可選) 獲取最新的消息用於聊天列表顯示
     */
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestMessageForConversation(conversationId: String): Message?

    // 可以添加更多方法，如更新消息狀態 (isRead, isSent) 等
    // @Query("UPDATE messages SET isRead = 1 WHERE id = :messageId")
    // suspend fun markMessageAsRead(messageId: String)
}