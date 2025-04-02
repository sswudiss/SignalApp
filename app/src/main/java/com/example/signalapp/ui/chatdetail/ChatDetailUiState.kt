package com.example.signalapp.ui.chatdetail

/**
 * 聊天詳情頁的 UI 狀態
 */
data class ChatDetailUiState(
    val messages: List<ChatMessage> = emptyList(), // 消息列表
    val currentInput: String = "",       // 當前輸入框內容
    val isLoading: Boolean = true,       // 是否正在加載初始消息
    val errorMessage: String? = null,   // 錯誤消息
    val chatPartnerName: String = ""    // 聊天對象的顯示名稱
    // 你可以在這裡添加更多狀態，例如發送失敗標誌等
    // val sendError: String? = null
)