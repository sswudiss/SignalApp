package com.example.signalapp.screens.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.signalapp.model.Message
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChatDetailViewModel : ViewModel() {

    // 可觀察的輸入框文本狀態
    var inputText by mutableStateOf("")
        private set

    // 可觀察的消息列表狀態 (使用 mutableStateListOf 可以在添加/刪除時觸發 recomposition)
    val messages = mutableStateListOf<Message>()

    // 當前聊天對象的 ID 和名稱 (需要從外部設置或加載)
    var contactId by mutableStateOf("")
        private set
    var contactName by mutableStateOf("載入中...")
        private set

    // 加載狀態
    var isLoading by mutableStateOf(false)
        private set

    // 更新輸入框文本
    fun onInputChange(newText: String) {
        inputText = newText
    }

    // 加載聊天記錄 (在實際應用中會從數據庫或網絡加載)
    fun loadMessages(id: String) {
        if (contactId == id && messages.isNotEmpty()) return // 避免重複加載

        contactId = id
        isLoading = true
        viewModelScope.launch {
            // 模擬網絡延遲或數據庫查詢
            delay(500)

            // 模擬加載聯繫人名稱
            contactName = when(id) {
                "user1" -> "Alice"
                "user2" -> "Bob"
                "group1" -> "Project Team"
                "self_user_id" -> "給自己的消息" // 處理自己的 ID
                else -> "未知聯繫人 $id"
            }

            // 清空舊消息並加載模擬數據
            messages.clear()
            val loadedMessages = generateSampleMessages(id) // 生成模擬數據
            messages.addAll(loadedMessages)
            isLoading = false
        }
    }

    // 發送消息
    fun sendMessage() {
        if (inputText.isBlank()) return // 不發送空消息

        val newMessage = Message(
            text = inputText.trim(),
            senderId = "me" // 假設 "me" 代表當前用戶
        )
        messages.add(newMessage) // 直接添加到列表末尾 (LazyColumn 會反轉)
        inputText = "" // 清空輸入框

        // TODO: 在實際應用中，這裡需要將消息持久化 (Room) 並發送到後端服務器

        // 模擬對方回复 (僅用於演示)
        viewModelScope.launch {
            delay(1500)
            val replyText = "收到: \"${newMessage.text}\""
            if (contactId != "self_user_id") { // 如果不是給自己發消息
                val replyMessage = Message(
                    text = replyText,
                    senderId = contactId // 對方發來的消息
                )
                messages.add(replyMessage)
            }
        }
    }

    // --- 模擬數據生成 ---
    private fun generateSampleMessages(contactId: String): List<Message> {
        return listOf(
            Message(text = "你好啊！", senderId = contactId, timestamp = System.currentTimeMillis() - 60000 * 10),
            Message(text = "Hi! 最近怎麼樣?", senderId = "me", timestamp = System.currentTimeMillis() - 60000 * 9),
            Message(text = "還不錯，你呢？ 這個週末有什麼計劃嗎？我們去爬山怎麼樣？", senderId = contactId, timestamp = System.currentTimeMillis() - 60000 * 8),
            Message(text = "聽起來不錯！我看看天氣預報。", senderId = "me", timestamp = System.currentTimeMillis() - 60000 * 5),
            Message(text = "週六好像天氣很好！那就這麼定了？", senderId = contactId, timestamp = System.currentTimeMillis() - 60000 * 2),
            Message(text = "OK! 週六早上 8 點山腳下見！", senderId = "me", timestamp = System.currentTimeMillis() - 60000 * 1)
        ).filter { contactId != "self_user_id" || it.senderId == "me" } // 如果是自己，只顯示自己發的
            .sortedBy { it.timestamp } // 按時間排序
    }

    // --- 按鈕點擊處理 (佔位符) ---
    fun onAddAttachment() {
        println("Add Attachment clicked")
        // TODO: 顯示選擇菜單 (圖片、文件、聯繫人)
    }

    fun onCameraClick() {
        println("Camera clicked")
        // TODO: 啟動相機
    }

    fun onVoiceClick() {
        println("Voice clicked")
        // TODO: 開始錄音
    }

    fun onVideoCallClick() {
        println("Video Call clicked for $contactId")
        // TODO: 發起視頻通話
    }

    fun onMenuClearHistory() {
        println("Clear History clicked")
        messages.clear() // 臨時清除內存中的消息
        // TODO: 在實際應用中需要清除數據庫記錄
    }
    fun onMenuDisappearingMessages() {
        println("Disappearing Messages clicked")
        // TODO: 實現自動銷毀消息設置
    }
    fun onMenuFontSize() {
        println("Font Size clicked")
        // TODO: 實現字體大小設置
    }
}