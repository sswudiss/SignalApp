package com.example.signalapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await // 需要 implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3") 或更高版本

// --- 定義 UI 狀態的數據類 ---
data class AuthUiState(
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "", // 僅註冊時使用
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val navigateToMainApp: Boolean = false, // 觸發導航到主應用
    val showRegistrationWarning: Boolean = true // 控制註冊警告的顯示（雖然在 UI 上寫死了，但理論上可由 VM 控制）
)

class AuthViewModel : ViewModel() {

    // Firebase 實例
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance() // 使用預設區域或你在初始化時設定的區域

    // 內部使用的假域名
    private val DUMMY_DOMAIN = "@signalclone.local" // 你可以自定義這個域名

    // --- UI 狀態流 ---
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // --- 更新 UI 輸入的函數 ---
    fun updateUsername(input: String) {
        _uiState.update { it.copy(username = input.trim(), errorMessage = null) } // 清除錯誤訊息當用戶開始輸入時
    }

    fun updatePassword(input: String) {
        _uiState.update { it.copy(password = input, errorMessage = null) }
    }

    fun updateConfirmPassword(input: String) {
        _uiState.update { it.copy(confirmPassword = input, errorMessage = null) }
    }

    // --- 清除錯誤訊息 ---
    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // --- 重置導航標誌 ---
    // 這個函數應該在導航實際發生後被調用
    fun navigationCompleted() {
        _uiState.update { it.copy(navigateToMainApp = false) }
    }


    // --- 註冊邏輯 ---
    fun registerUser() {
        val currentState = _uiState.value
        val username = currentState.username
        val password = currentState.password
        val confirmPassword = currentState.confirmPassword

        // 1. 基本驗證
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "用戶名和密碼不能為空") }
            return
        }
        if (password.length < 6) { // Firebase 預設密碼最小長度為 6
            _uiState.update { it.copy(errorMessage = "密碼長度至少需要 6 位") }
            return
        }
        if (password != confirmPassword) {
            _uiState.update { it.copy(errorMessage = "兩次輸入的密碼不一致") }
            return
        }

        // 設置為加載狀態
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                // 2. 檢查用戶名唯一性 (Realtime Database)
                val usernameRef = database.getReference("usernames").child(username)
                val usernameSnapshot = usernameRef.get().await()

                if (usernameSnapshot.exists()) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "用戶名 \"${username}\" 已被使用") }
                    return@launch
                }

                // 3. 創建 Firebase Auth 用戶
                val firebaseEmail = username + DUMMY_DOMAIN
                val authResult = firebaseAuth.createUserWithEmailAndPassword(firebaseEmail, password).await()

                // 4. 註冊成功，將用戶名映射寫入 Realtime Database
                val uid = authResult.user?.uid
                if (uid != null) {
                    // 使用 Map 原子性寫入多個位置 (可選，但更好)
                    val userDataUpdates = mapOf(
                        "/usernames/$username" to uid,
                        "/users/$uid/username" to username
                    )
                    database.reference.updateChildren(userDataUpdates).await() // 等待寫入完成

                    // 更新狀態觸發導航
                    _uiState.update { it.copy(isLoading = false, navigateToMainApp = true) }
                } else {
                    // 雖然理論上 authResult 成功後 uid 不會為 null，但做個防護
                    _uiState.update { it.copy(isLoading = false, errorMessage = "無法獲取用戶UID，請重試") }
                }

            } catch (e: FirebaseAuthWeakPasswordException) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "密碼太弱，請使用更複雜的密碼") }
            } catch (e: FirebaseAuthUserCollisionException) {
                // 理論上我們已經檢查過 username，這個錯誤不應發生在 Email 碰撞上，除非 DUMMY_DOMAIN 選擇不佳
                // 但也可能是內部錯誤或其他情況
                _uiState.update { it.copy(isLoading = false, errorMessage = "該帳號似乎已存在 (錯誤碼: Collision)") }
            } catch (e: FirebaseAuthException) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "註冊失敗: ${e.localizedMessage ?: "未知 Firebase Auth 錯誤"}") }
            } catch (e: Exception) {
                // 處理數據庫寫入錯誤或其他異常
                _uiState.update { it.copy(isLoading = false, errorMessage = "註冊失敗: ${e.localizedMessage ?: "未知錯誤"}") }
                // 可選：如果 Auth 創建成功但數據庫寫入失敗，可能需要考慮回滾 Auth 帳號（較複雜）
            }
        }
    }


    // --- 登入邏輯 ---
    fun loginUser() {
        val currentState = _uiState.value
        val username = currentState.username
        val password = currentState.password

        // 1. 基本驗證
        if (username.isEmpty() || password.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "用戶名和密碼不能為空") }
            return
        }

        // 設置為加載狀態
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            try {
                // 2. 構造假 Email 並嘗試登入
                val firebaseEmail = username + DUMMY_DOMAIN
                firebaseAuth.signInWithEmailAndPassword(firebaseEmail, password).await()

                // 3. 登入成功，更新狀態觸發導航
                _uiState.update { it.copy(isLoading = false, navigateToMainApp = true) }

            } catch (e: FirebaseAuthException) {
                // 更友好的錯誤提示，而不是直接顯示 Firebase 的錯誤
                _uiState.update { it.copy(isLoading = false, errorMessage = "用戶名或密碼錯誤") }
                // Log.e("AuthViewModel", "Login failed: ${e.errorCode} - ${e.message}") // 可選：記錄詳細錯誤
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "登入失敗: ${e.localizedMessage ?: "未知錯誤"}") }
            }
        }
    }

    // 注意：登出邏輯通常不在 AuthViewModel 中觸發導航回登入頁，
    // 而是由 Activity/NavController 監聽 FirebaseAuth.authStateChanges 來完成。
    // 但我們可以提供登出方法本身。
    fun logoutUser() {
        firebaseAuth.signOut()
        // 可以選擇性地重置 ViewModel 狀態
        _uiState.value = AuthUiState() // 重置為初始狀態
    }
}

/*
* 說明：
AuthUiState Data Class: 定義了一個單一的數據類來持有所有相關的 UI 狀態，這有助於管理複雜性。
_uiState 和 uiState: 使用 MutableStateFlow 作為內部可變狀態，並將其作為 StateFlow 暴露給外部 (UI)。UI 層應該只讀取 uiState。
viewModelScope: 所有異步操作（Firebase 調用）都在 viewModelScope 中啟動，確保它們與 ViewModel 的生命周期綁定，在 ViewModel 清除時會自動取消。
updateX 函數: 提供給 UI 調用的方法，用於更新 ViewModel 中的狀態 (username, password 等)。在更新時清除了 errorMessage 以改善用戶體驗。
registerUser() 邏輯：

執行客戶端驗證。
設置 isLoading = true。
異步檢查用戶名： 使用 database.getReference(...).get().await() 異步獲取數據庫快照。await() 是 Kotlin 協程對 Google Play Services Task 的擴展函數，
* 需要添加 kotlinx-coroutines-play-services 依賴。
*
創建 Auth 用戶： 調用 createUserWithEmailAndPassword(fakeEmail, password).await()。
寫入數據庫映射： 如果 Auth 創建成功，使用 updateChildren (更優) 或 setValue 將用戶名和 UID 寫入 Realtime Database。
處理錯誤： 使用 try-catch 捕獲特定的 Firebase Auth 異常 (FirebaseAuthWeakPasswordException, FirebaseAuthUserCollisionException) 和一般異常，並更新 errorMessage。
更新狀態： 無論成功或失敗，最後都要更新 isLoading = false 和 navigateToMainApp 或 errorMessage。
loginUser() 邏輯：
執行客戶端驗證。
設置 isLoading = true。
調用 signInWithEmailAndPassword(fakeEmail, password).await()。
處理成功和失敗情況，更新 UI 狀態。登入失敗時，給出統一的 "用戶名或密碼錯誤" 提示，避免透露帳號是否存在。
logoutUser(): 提供登出的方法，主要就是調用 firebaseAuth.signOut()。
navigationCompleted(): 提供一個方法讓 UI 在完成導航後調用，以重置 navigateToMainApp 標誌，防止重複導航。
clearErrorMessage(): 允許 UI 主動清除錯誤消息（例如，當用戶開始重新輸入時）。
* */