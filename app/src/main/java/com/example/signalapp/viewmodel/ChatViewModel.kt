// viewmodel/ChatViewModel.kt
package com.example.signalapp.viewmodel

import android.util.Log // 用於日誌記錄
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.signalapp.AppDestinations
import com.example.signalapp.data.ChatMessage
import com.google.firebase.database.* // 導入 Firebase Database
import com.google.firebase.database.ktx.database // KTX 擴展
import com.google.firebase.database.ktx.getValue // KTX 擴展
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


// --- 添加 Firebase 連接狀態 ---
enum class ConnectionStatus {
    CONNECTED,
    CONNECTING,
    DISCONNECTED,
    ERROR
}

// --- ChatUiState data class 保持不變 ---
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "", // 改回由 ViewModel 管理 inputText
    val conversationName: String = "聊天",
    val isLoading: Boolean = true, // 初始設為 true
    val error: String? = null,
    val connectionStatus: ConnectionStatus = ConnectionStatus.CONNECTING // 初始狀態為連接中
)

class ChatViewModel(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val conversationId: String =
        savedStateHandle.get<String>(AppDestinations.CHAT_ID_KEY) ?: "unknown"
    private val database = Firebase.database // 獲取 Firebase Realtime Database 實例
    private lateinit var messagesRef: DatabaseReference // 資料庫中對應此聊天的引用
    private var messagesListener: ChildEventListener? = null // 監聽器引用，方便移除

    // 添加對 Firebase .info/connected 的引用
    private val connectedRef = database.getReference(".info/connected")
    private var connectionListener: ValueEventListener? = null // 連接狀態監聽器

    // --- 狀態管理 (UI State 包含 inputText) ---
    private val _uiState = MutableStateFlow(ChatUiState(isLoading = true)) // 初始 isLoading = true
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    // 模擬當前用戶 ID (之後會用 Firebase Auth 替換)
    private val currentUserId = "currentUser" // FIXME: 臨時硬編碼

    init {
        println("ChatViewModel initialized for conversation ID: $conversationId")
        if (conversationId != "unknown") {
            // 構建到特定聊天訊息的資料庫路徑
            messagesRef = database.getReference("messages").child(conversationId)
            // 模擬加載聊天名稱 (之後也可以從 Firebase 讀取)
            simulateLoadConversationName(conversationId)
            // 可以保留或移除
            // 開始監聽連接狀態
            attachConnectionListener()
            // 開始監聽訊息 (在確認連接後或並行進行)
            attachMessagesListener()
        } else {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "無效的對話 ID",
                    connectionStatus = ConnectionStatus.ERROR
                )
            }
        }

        Log.d("ChatViewModel", "Conversation ID: $conversationId")
    }


    // --- 連接狀態監聽 ---
    private fun attachConnectionListener() {
        detachConnectionListener() // 先移除舊的，防止重複添加
        connectionListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                val newStatus =
                    if (connected) ConnectionStatus.CONNECTED else ConnectionStatus.DISCONNECTED
                Log.d("ChatViewModel", "Firebase connection status: $newStatus")
                _uiState.update {
                    it.copy(
                        connectionStatus = newStatus,
                        // 如果是斷開連接，可能需要顯示一個錯誤或提示
                        error = if (!connected && it.error == null) "連接已斷開" else it.error,
                        // 如果剛連上，清除連接錯誤提示 (如果有的話)
                        // error = if (connected && it.error == "連接已斷開") null else it.error
                        isLoading = if (connected && it.isLoading && it.messages.isEmpty()) true else false // 連接上後如果還在初始加載狀態，保持isLoading
                    )
                }
                // 如果是首次連接成功，且訊息監聽器尚未加載數據，確保觸發加載
                if (connected && messagesListener == null) {
                    Log.d(
                        "ChatViewModel",
                        "Connection established, ensuring message listener is attached."
                    )
                    attachMessagesListener() // 確保訊息監聽已啟動
                } else if (!connected) {
                    // 斷線時，可能需要處理 message listener 的狀態 (例如顯示最後的緩存數據，如果有的話)
                    // 目前 ChildEventListener 在斷線重連後會自動處理數據同步
                    _uiState.update { it.copy(isLoading = false) } // 斷線時，停止任何進行中的加載狀態
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatViewModel", "Firebase connection listener cancelled: ${error.message}")
                _uiState.update {
                    it.copy(
                        connectionStatus = ConnectionStatus.ERROR,
                        error = "無法監聽連接狀態: ${error.message}"
                    )
                }
            }
        }
        connectedRef.addValueEventListener(connectionListener!!)
    }

    private fun detachConnectionListener() {
        connectionListener?.let { connectedRef.removeEventListener(it) }
        connectionListener = null
    }

    // --- 訊息發送 (添加連接檢查) ---
    fun sendMessage() {
        // 檢查是否已連接
        if (_uiState.value.connectionStatus != ConnectionStatus.CONNECTED) {
            _uiState.update { it.copy(error = "未連接到伺服器，無法發送訊息") }
            Log.w("ChatViewModel", "Send message attempt while disconnected.")
            return // 直接返回，不執行發送
        }

        val textToSend = _uiState.value.inputText
        if (textToSend.isNotBlank()) {
            val message = ChatMessage( /* ... */)
            messagesRef.push().setValue(message)
                .addOnSuccessListener {
                    println("Message sent successfully!")
                    _uiState.update { it.copy(inputText = "", error = null) } // 清除輸入和之前的錯誤
                }
                .addOnFailureListener { e ->
                    println("Error sending message: $e")
                    // 区分是權限問題還是網絡問題 (Firebase SDK 可能會自動處理瞬時網絡問題)
                    _uiState.update { it.copy(error = "發送失敗: ${e.message}") }
                }
        }
    }


    // --- 事件處理 ---
    fun updateTextField(newValue: String) {
        _uiState.update { it.copy(inputText = newValue) }
    }


    // --- 數據加載 (Firebase Listener) ---
    private fun attachMessagesListener() {
        // 清理舊的監聽器（如果有的話）
        detachMessagesListener()

        _uiState.update { it.copy(isLoading = true, error = null) }

        messagesListener = object : ChildEventListener {
            // 當有新子節點 (新訊息) 添加時觸發
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                // 將 Firebase Snapshot 轉換為 ChatMessage 物件
                val message = snapshot.getValue<ChatMessage>() // 使用 KTX 擴展
                if (message != null) {
                    // 使用 Firebase 生成的 key 作為 ID
                    val messageWithId = message.copy(id = snapshot.key ?: message.id)
                    _uiState.update { currentState ->
                        // 避免重複添加 (雖然 ChildEventListener 通常不會)
                        if (currentState.messages.none { it.id == messageWithId.id }) {
                            // 添加新訊息到列表，並按時間戳排序 (可選，如果依賴 reverseLayout)
                            val updatedMessages = (currentState.messages + messageWithId)
                                .sortedBy { it.timestamp } // 確保按時間排序
                            currentState.copy(messages = updatedMessages, isLoading = false)
                        } else {
                            currentState.copy(isLoading = false) // 數據已存在，停止加載狀態
                        }
                    }
                    Log.d("ChatViewModel", "Message added: ${messageWithId.text}")
                } else {
                    Log.w("ChatViewModel", "Received null message from snapshot: ${snapshot.key}")
                    _uiState.update { it.copy(isLoading = false) } // 數據無效，停止加載
                }
            }

            // 當子節點 (訊息) 內容改變時觸發 (例如編輯訊息功能)
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val updatedMessage = snapshot.getValue<ChatMessage>()
                if (updatedMessage != null) {
                    val messageWithId = updatedMessage.copy(id = snapshot.key ?: updatedMessage.id)
                    _uiState.update { currentState ->
                        currentState.copy(
                            messages = currentState.messages.map {
                                if (it.id == messageWithId.id) messageWithId else it
                            }.sortedBy { it.timestamp }, // 維持排序
                            isLoading = false // 可能需要更新 isLoading 狀態
                        )
                    }
                    Log.d("ChatViewModel", "Message changed: ${messageWithId.text}")
                }
            }

            // 當子節點 (訊息) 被移除時觸發
            override fun onChildRemoved(snapshot: DataSnapshot) {
                val removedMessageId = snapshot.key
                if (removedMessageId != null) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            messages = currentState.messages.filterNot { it.id == removedMessageId }
                            // 不需要重新排序，filter 不改變順序
                        )
                    }
                    Log.d("ChatViewModel", "Message removed: $removedMessageId")
                }
            }

            // 當子節點列表順序改變時觸發 (在 Realtime DB 中不常用，除非使用 priority)
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // 一般聊天應用可以忽略
                Log.d("ChatViewModel", "Message moved: ${snapshot.key}")
            }

            // 當監聽被取消或權限不足時觸發
            override fun onCancelled(error: DatabaseError) {
                Log.e("ChatViewModel", "Firebase listener cancelled: ${error.message}")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "數據庫監聽錯誤: ${error.message}"
                    )
                }
            }
        }
        // 將監聽器附加到資料庫引用
        messagesRef.addChildEventListener(messagesListener!!)
    }

    // 移除監聽器，防止內存洩漏
    private fun detachMessagesListener() {
        messagesListener?.let {
            messagesRef.removeEventListener(it)
        }
        messagesListener = null
    }

    // --- 模擬函數 (聊天名稱，之後可以改為從 Firebase 讀取) ---
    private fun simulateLoadConversationName(convId: String) {
        viewModelScope.launch {
            kotlinx.coroutines.delay(100) // 模擬延遲
            val name = when (convId) {
                "user123" -> "Alice (Realtime)"
                "user456" -> "Bob (Realtime)"
                "group789" -> "學習小組 (Realtime)"
                else -> "未知對話"
            }
            _uiState.update { it.copy(conversationName = name) }
        }
    }

    // --- ViewModel 銷毀時清理兩個監聽器 ---
    override fun onCleared() {
        super.onCleared()
        Log.d("ChatViewModel", "ViewModel cleared, detaching listeners for $conversationId")
        detachMessagesListener()
        detachConnectionListener() // <-- 清理連接監聽器
    }
}