package com.example.signalapp.ui.chatdetail

data class ChatDetailUiState(
    val messages: List<ChatMessage> = emptyList(),
    val currentInput: String = "",
    val isLoading: Boolean = true, // 仍然用於初始加載狀態
    val errorMessage: String? = null,
    val chatPartnerName: String = ""
)