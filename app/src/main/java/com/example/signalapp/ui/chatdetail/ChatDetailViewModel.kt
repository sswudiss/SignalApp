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
    // Hilt 自動注入 SavedStateHandle 來獲取導航參數
    private val savedStateHandle: SavedStateHandle,
    // Hilt 注入 Repository
    private val chatRepository: ChatRepository,
    // Hilt 注入 UserRepository (臨時用於獲取名字，更好的方案待定)
    private val userRepository: UserRepository // 如果不再需要模擬名字，可以移除
) : ViewModel() {

    // 從導航參數中獲取當前聊天的 ID
    // 使用 !! 假設 chatId 肯定存在 (由 NavHost 保證)
    private val chatId: String = savedStateHandle.get<String>(NavigationArgs.CHAT_ID)!!

    // 內部可變的 UI 狀態 Flow
    private val _uiState = MutableStateFlow(ChatDetailUiState(isLoading = true))
    // 向外部 UI 暴露的只讀 UI 狀態 Flow
    val uiState: StateFlow<ChatDetailUiState> = _uiState.asStateFlow()

    init {
        // ViewModel 初始化時，開始加載夥伴名稱和監聽消息
        loadPartnerName()
        collectMessages()
    }

    /**
     * 異步加載聊天夥伴的名稱並更新 UI 狀態。
     * 這是臨時方案，應由更合適的數據源提供。
     */
    private fun loadPartnerName() {
        viewModelScope.launch {
            // --- 模擬或從數據庫獲取名字的邏輯 ---
            // 理想情況下: chatRepository.getChatMetadata(chatId)?.participantName
            // 或 userRepository.getUser(chatId)?.displayName 等
            val partnerName = getSimulatedPartnerName(chatId) // 使用模擬函數
            // 更新狀態中的 chatPartnerName
            _uiState.update { currentState ->
                currentState.copy(chatPartnerName = partnerName)
            }
        }
    }

    /**
     * 開始收集來自數據庫的消息流，並在收到更新時更新 UI 狀態。
     */
    private fun collectMessages() {
        viewModelScope.launch {
            chatRepository.getMessagesForChat(chatId) // 獲取 Flow<List<ChatMessage>>
                .catch { throwable -> // 捕獲來自 Flow 的錯誤
                    println("錯誤：加載聊天 $chatId 的消息失敗: ${throwable.message}")
                    _uiState.update { currentState ->
                        currentState.copy(
                            errorMessage = "無法加載消息",
                            isLoading = false // 停止加載狀態
                        )
                    }
                }
                .collect { messageList -> // 當有新消息列表時
                    // 更新 UI 狀態中的消息列表
                    _uiState.update { currentState ->
                        currentState.copy(
                            messages = messageList,
                            isLoading = false, // 數據已加載，停止加載狀態
                            errorMessage = null // 清除之前的錯誤
                        )
                    }
                }
        }
    }

    /**
     * 當 UI 輸入框內容變化時調用此方法。
     * @param newInput 新的輸入文本。
     */
    fun updateInput(newInput: String) {
        _uiState.update { currentState ->
            currentState.copy(currentInput = newInput)
        }
    }

    /**
     * 當用戶點擊發送按鈕時調用此方法。
     */
    fun sendMessage() {
        // 從當前狀態獲取要發送的文本，去除首尾空格
        val textToSend = _uiState.value.currentInput.trim()
        // 如果文本為空，則不執行任何操作
        if (textToSend.isBlank()) {
            return
        }

        // 立即清空 UI 上的輸入框，提供快速反饋
        _uiState.update { currentState ->
            currentState.copy(currentInput = "")
        }

        // 啟動一個協程來處理消息的保存
        viewModelScope.launch {
            try {
                // --- 獲取當前用戶 ID ---
                // 這部分需要一個真實的用戶認證/會話管理機制
                // 暫時硬編碼為 "currentUser"
                val currentUserId = "currentUser"

                // 調用 Repository 保存新消息
                chatRepository.saveNewMessage(
                    chatId = chatId,
                    text = textToSend,
                    senderId = currentUserId,
                    isSentByCurrentUser = true // 從 ViewModel 發送的總是當前用戶
                )
                // 不需要手動更新 UI 的 messages 列表，collectMessages 會自動處理
                println("消息已請求保存到數據庫: $textToSend")

            } catch (e: Exception) { // 捕獲保存過程中可能發生的異常
                println("錯誤：保存消息到數據庫失敗: ${e.message}")
                // TODO: 可以更新 UI 狀態來顯示發送失敗的提示
                // _uiState.update { it.copy(sendError = "發送失敗") }
            }
        }
    }

    /**
     * 模擬獲取聊天對象名字的輔助函數。
     * TODO: 替換為從真實數據源（如 UserRepository 或 ChatRepository）獲取。
     */
    private fun getSimulatedPartnerName(chatId: String): String {
        // 簡單模擬，實際應從數據庫或聯繫人列表獲取
        return when (chatId) {
            "Alice_id" -> "Alice" // 示例 ID
            "Bob_id" -> "Bob The Builder" // 示例 ID
            else -> "用戶 $chatId" // 默認顯示
        }
    }

    // 注意：沒有 refreshMessages() 方法，因為使用了響應式 Flow。
}