package com.example.signalapp.ui.chatlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.signalapp.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val chatRepository: ChatRepository // <--- 注入 ChatRepository) : ViewModel()
)  : ViewModel(){

    // 直接將 Repository 的 Flow 轉換為 StateFlow<ChatListUiState>
    val uiState: StateFlow<ChatListUiState> = chatRepository.getChatSummaries() // 從 Repo 獲取 Flow<List<ChatSummary>>
        .map { chatList ->
            ChatListUiState(chats = chatList, isLoading = false) // 轉換為包含列表的 UI State
        }
        .catch { throwable ->
            // 如果從數據庫 Flow 中捕獲到異常
            println("Error loading chat list: ${throwable.message}")
            // 發出一個包含錯誤信息的 UI State
            emit(ChatListUiState(isLoading = false, errorMessage = "無法加載聊天列表"))
        }
        .stateIn(
            scope = viewModelScope, // 在 ViewModel 的生命週期內共享
            // 當沒有活躍收集者時，等待 5 秒後停止共享；再次收集時重新啟動上游 Flow
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = ChatListUiState(isLoading = true) // 初始狀態：正在加載
        )

    // --- 移除舊的 loadChatList 方法和假數據相關代碼 ---
    /*
    private val _uiState = MutableStateFlow(ChatListUiState())
    val uiState: StateFlow<ChatListUiState> = _uiState.asStateFlow()
    init { loadChatList() }
    private fun loadChatList() { ... }
    private fun generateDummyMessages(...) { ... }
    private fun getPartnerName(...) { ... }
    */

    // 可以添加下拉刷新等操作觸發的方法
    // fun refresh() { /* 可能需要觸發 Repository 重新查詢或網絡同步 */ }
}