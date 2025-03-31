package com.example.signalapp.data

import androidx.compose.runtime.Immutable  // (可選) 標記為不可變，可能有助於 Compose 優化

/*定義訊息資料模型
data class ChatMessage(
    val id: String, // 訊息的唯一 ID
    val text: String, // 訊息內容
    val senderId: String, // 發送者 ID
    val timestamp: Long, // 訊息時間戳 (例如 System.currentTimeMillis())
    val isSentByCurrentUser: Boolean // 這條訊息是否是當前用戶發送的
)
*/

//定義訊息資料模型
@Immutable // (可選)
data class ChatMessage(
    val id: String = "", // 訊息唯一 ID (Firebase key)
    val text: String = "", // 訊息內容
    val senderId: String = "", // 發送者 ID (稍後會是 Firebase Auth User ID)
    val timestamp: Long = 0L // 訊息時間戳 (伺服器時間或客戶端時間)
    // 注意：isSentByCurrentUser 不再儲存，在 UI 層根據 senderId 判斷
)