package com.example.signalapp.ui.newchat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.signalapp.data.local.entity.UserEntity
import com.example.signalapp.data.repository.ChatRepository // 需要用於創建聊天
import com.example.signalapp.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewChatViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository // 注入 ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewChatUiState())
    val uiState: StateFlow<NewChatUiState> = _uiState.asStateFlow()

    // 需要知道當前用戶名才能排除自己
    // 實際應用中這個應該來自登錄狀態管理，這裡暫時硬編碼
    private val currentUsername = "currentUser" // <<--- 警告：硬編碼！

    init {
        loadContacts()
    }

    private fun loadContacts() {
        viewModelScope.launch {
            userRepository.getOtherUsersStream(currentUsername)
                .map { userEntityList -> // 將 UserEntity 映射為 ContactUiModel
                    userEntityList.map { user ->
                        ContactUiModel(username = user.username, displayName = user.username)
                    }
                }
                .onStart { _uiState.update { it.copy(isLoading = true, error = null) } }
                .catch { throwable ->
                    println("Error loading contacts: ${throwable.message}")
                    _uiState.update { it.copy(isLoading = false, error = "無法加載聯繫人") }
                }
                .collect { contactList ->
                    _uiState.update {
                        it.copy(contacts = contactList, isLoading = false)
                    }
                }
        }
    }

    /**
     * 當用戶選擇一個聯繫人時調用。
     * @param contactUsername 被選中聯繫人的用戶名 (這也將是 chatId)
     */
    fun onContactSelected(contactUsername: String) {
        viewModelScope.launch {
            // 可以在這裡檢查聊天是否已存在，但 ChatRepository 的 saveNewMessage 內部
            // 也有創建邏輯，所以也許不需要在這裡預先創建摘要。
            // 主要目的是確保導航到正確的 chatId。

            // 為了演示，我們可以在 Repository 層增加一個 "ensureChatExists" 方法
            // 或者就在這裡觸發導航
            _uiState.update { it.copy(navigateToChatId = contactUsername) }
        }
    }

    /**
     * UI 導航完成後調用此方法重置導航狀態。
     */
    fun navigationDone() {
        _uiState.update { it.copy(navigateToChatId = null) }
    }
}