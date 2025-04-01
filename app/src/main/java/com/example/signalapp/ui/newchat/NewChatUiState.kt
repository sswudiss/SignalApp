package com.example.signalapp.ui.newchat


data class NewChatUiState(
    val contacts: List<ContactUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val navigateToChatId: String? = null // 用於觸發導航的狀態
)