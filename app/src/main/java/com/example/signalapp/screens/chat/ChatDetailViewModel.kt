package com.example.signalapp.screens.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.signalapp.db.MessageDao
import com.example.signalapp.model.Message
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel // 標註這是一個 Hilt ViewModel
class ChatDetailViewModel @Inject constructor(
    private val messageDao: MessageDao  // Hilt 會自動從 DatabaseModule 提供 MessageDao
) : ViewModel() {  // 使用 @Inject 標註構造函數


    /*
    * @HiltViewModel: 添加此註解，告訴 Hilt 這個 ViewModel 需要進行依賴注入。
@Inject constructor(...): 在 ViewModel 的主構造函數前添加 @Inject。
Hilt 將查找如何提供構造函數所需的所有參數（這裡是 MessageDao）。因為我們在 DatabaseModule 中提供了 MessageDao，Hilt 就知道如何創建它。
不再需要 Factory: 有了 @HiltViewModel 和 @Inject，我們不再需要手動創建 ChatDetailViewModelFactory 了。
    * */

    private val currentUserId: String? = FirebaseAuth.getInstance().currentUser?.uid // 獲取當前用戶 ID

    // 輸入框文本狀態
    var inputText by mutableStateOf("")
        private set

    // 當前對話的 ID (例如對方 ID 或自己的 ID)
    private val _conversationId = MutableStateFlow("") // 使用 StateFlow 存儲 ID

    // (可選) 聯繫人名稱 - 暫時還用簡單邏輯，後續應從 Contact 表加載
    var contactName by mutableStateOf("載入中...")
        private set

    // 加載狀態
    var isLoading by mutableStateOf(false) // 可以用 Flow 的狀態來管理，這裡暫時保留

    // --- 使用 Flow 從 Room 讀取消息 ---
    @OptIn(ExperimentalCoroutinesApi::class) // 需要 flatMapLatest
    val messages: StateFlow<List<Message>> = _conversationId
        .filter { it.isNotBlank() } // 確保 conversationId 不為空才開始查詢
        .flatMapLatest { conversationId ->
            // 當 conversationId 變化時，切換到對應的數據庫查詢 Flow
            messageDao.getMessagesForConversation(conversationId)
        }
        // 在 viewModelScope 中啟動 Flow，並轉換為 StateFlow 以便 Compose 觀察
        // 使用 stateIn 提供初始值並使其在沒有觀察者時保持活躍一段時間（或永久）
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // 訂閱者停止後 5 秒停止 Flow
            initialValue = emptyList() // 初始值為空列表
        )


    fun onInputChange(newText: String) {
        inputText = newText
    }

    // 不再需要單獨的加載函數，設置 conversationId 就會觸發 Flow 更新
    fun setConversationId(id: String) {
        if (_conversationId.value == id) return // 如果 ID 沒變，不處理
        _conversationId.value = id
        isLoading = true // 開始加載時設置 loading，雖然 Flow 自己會更新
        // 加載聯繫人名稱 (仍然是模擬)
        viewModelScope.launch {
            delay(100) // 短暫延遲模擬加載
            contactName = when (id) {
                "user1" -> "Alice"
                "user2" -> "Bob"
                "group1" -> "Project Team"
                currentUserId ?: "self" -> "給自己的消息" // 使用真實用戶 ID 判斷是否是自己
                else -> "未知 $id"
            }
            isLoading = false // 名字加載完成
        }
        // 同時可以觸發一次性的歷史消息獲取或其他操作（如果需要的話）
        // loadInitialHistory(id)
    }

    // 發送消息
    fun sendMessage() {
        if (inputText.isBlank() || currentUserId == null || _conversationId.value.isBlank()) return

        val conversationId = _conversationId.value // 獲取當前對話 ID

        val newMessage = Message(
            text = inputText.trim(),
            senderId = currentUserId, // 發送者是當前用戶
            conversationId = conversationId // 設置正確的 conversationId
        )

        // 使用 viewModelScope 啟動協程來插入數據庫
        viewModelScope.launch {
            messageDao.insertMessage(newMessage)
            // Room Flow 會自動觸發 messages StateFlow 的更新
            // 所以不需要手動 messages.add(newMessage)
        }

        // 清空輸入框
        inputText = ""

        // TODO: 將消息發送到後端/對方 (這是網絡部分)

        // 模擬對方回复不再需要，因為我們讀取數據庫
        // simulateReply()
    }


    // --- 菜單操作 ---
    fun onMenuClearHistory() {
        val conversationId = _conversationId.value
        if (conversationId.isNotBlank()) {
            viewModelScope.launch {
                messageDao.deleteMessagesForConversation(conversationId)
                // Room Flow 會自動更新 UI
            }
        }
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
        println("Video Call clicked for **")
        // TODO: 發起視頻通話
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