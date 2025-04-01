package com.example.signalapp.ui.chatdetail // <--- 包名已更新

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.signalapp.data.repository.ChatRepository // <--- 導入 ChatRepository
// 暫時保留 UserRepository 以獲取模擬名稱，未來應合併或重構
import com.example.signalapp.data.repository.UserRepository
import com.example.signalapp.navigation.NavigationArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.* // 導入 collect, onStart, catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatDetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository, // <--- 注入 ChatRepository
    private val userRepository: UserRepository  // <--- 注入 UserRepository (臨時用於名字)
) : ViewModel() {

    private val chatId: String = savedStateHandle.get<String>(NavigationArgs.CHAT_ID)!!

    private val _uiState = MutableStateFlow(ChatDetailUiState(isLoading = true)) // 初始狀態isLoading=true
    val uiState: StateFlow<ChatDetailUiState> = _uiState.asStateFlow()

    init {
        // 異步加載夥伴名稱
        loadPartnerName()
        // 開始收集消息流
        collectMessages()
    }

    // 異步加載名字
    private fun loadPartnerName() {
        viewModelScope.launch {
            // 從 UserRepository 獲取用戶名是一個模擬。
            // 更好的方法是 ChatRepository 提供一個基於 chatId 獲取對方信息的方法
            // 或者直接從 ChatSummaryEntity 獲取（如果存了）
            val partnerName = getSimulatedPartnerName(chatId) // 使用模擬方法
            _uiState.update { it.copy(chatPartnerName = partnerName) }
        }
    }


    // 收集消息 Flow
    private fun collectMessages() {
        viewModelScope.launch {
            chatRepository.getMessagesForChat(chatId) // 從 Repo 獲取 Flow<List<ChatMessage>>
                .onStart { /* 可以選擇在這裡觸發 _uiState.update { it.copy(isLoading = true) } */ }
                .catch { throwable ->
                    println("Error loading messages for chat $chatId: ${throwable.message}")
                    _uiState.update {
                        it.copy(errorMessage = "無法加載消息", isLoading = false)
                    }
                }
                .collect { messageList -> // 當數據庫有新消息時，這裡會被調用
                    _uiState.update {
                        it.copy(messages = messageList, isLoading = false, errorMessage = null) // 更新消息列表，清除加載和錯誤
                    }
                }
        }
    }

    fun updateInput(newInput: String) {
        // 更新輸入框文本，清除之前的發送錯誤 (如果有的話)
        _uiState.update { it.copy(currentInput = newInput/*, sendError = null*/) }
    }

    fun sendMessage() {
        val textToSend = _uiState.value.currentInput.trim()
        if (textToSend.isBlank()) return

        // 清空輸入框 (立即反饋)
        _uiState.update { it.copy(currentInput = "") }

        viewModelScope.launch {
            try {
                // 從 SavedStateHandle 或某個用戶管理服務獲取當前用戶 ID
                val currentUserId = "currentUser" // <--- 暫時硬編碼

                chatRepository.saveNewMessage(
                    chatId = chatId,
                    text = textToSend,
                    senderId = currentUserId,
                    isSentByCurrentUser = true // 這裡發送的都是當前用戶的
                )
                println("消息 '$textToSend' (to chat $chatId) 已請求保存到數據庫")
                // 消息會通過 collectMessages 的 Flow 自動更新到 UI
            } catch (e: Exception) {
                println("保存消息時出錯: ${e.message}")
                // 可以選擇在 UI 上顯示發送失敗的提示
                // _uiState.update { it.copy(sendError = "發送失敗") }
            }
        }
    }

    // --- 移除本地的假數據生成 ---
    /*
    private fun loadInitialMessages() { ... }
    private fun generateDummyMessages(...) { ... }
    private fun generateMessageText(...) { ... }
    */
    // 保持這個模擬函數直到有更好的數據源
    private fun getSimulatedPartnerName(chatId: String): String {
        // 簡單模擬，實際應從數據庫或聯繫人列表獲取
        return when (chatId) {
            "1" -> "Alice" // 與 ChatListViewModel 的假數據保持一致（如果有殘留）
            "2" -> "Bob The Builder"
            "3" -> "Charlie"
            "4" -> "David"
            "5" -> "Eve"
            "6" -> "Frank"
            else -> "聊天對象 $chatId"
        }
    }
}