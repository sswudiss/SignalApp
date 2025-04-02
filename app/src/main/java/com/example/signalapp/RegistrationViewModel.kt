package com.example.signalapp


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await // 需要添加 play-services-tasks 依賴（通常 Auth 會附帶）

// 定義註冊狀態
sealed class RegistrationState {
    object Idle : RegistrationState() // 初始狀態
    object Loading : RegistrationState() // 正在註冊
    object Success : RegistrationState() // 註冊成功
    data class Error(val message: String) : RegistrationState() // 註冊失敗
}

class RegistrationViewModel : ViewModel() {

    // Firebase Auth 實例
    // 注意：這裡直接獲取實例。之後使用 Hilt 時，我們會通過構造函數注入。
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // 使用 StateFlow 來暴露註冊狀態給 UI
    private val _registrationState = MutableStateFlow<RegistrationState>(RegistrationState.Idle)
    val registrationState: StateFlow<RegistrationState> = _registrationState

    // 註冊函數
    fun registerUser(username: String, password: String) {
        // 設置為加載狀態
        _registrationState.value = RegistrationState.Loading

        // 構建 Firebase 需要的郵箱格式
        val email = "$username@cruise.com"

        // 使用 viewModelScope 啟動一個協程來執行異步操作
        viewModelScope.launch {
            try {
                // 調用 Firebase Auth 的創建用戶方法
                auth.createUserWithEmailAndPassword(email, password).await() // 使用 await() 使其在協程中掛起

                // 註冊成功
                _registrationState.value = RegistrationState.Success
                // 可以在這裡添加其他成功後的邏輯，比如更新用戶資料等（如果需要）

            } catch (e: FirebaseAuthUserCollisionException) {
                // 用戶名（郵箱）已存在
                _registrationState.value = RegistrationState.Error("用戶名 '$username' 已經被註冊。")
            } catch (e: FirebaseAuthWeakPasswordException) {
                // 密碼太弱
                _registrationState.value = RegistrationState.Error("密碼太弱，請使用更強的密碼。")
            } catch (e: Exception) {
                // 其他未知錯誤
                _registrationState.value = RegistrationState.Error("註冊失敗：${e.localizedMessage ?: "未知錯誤"}")
                // 可以在 Logcat 中打印詳細錯誤，方便調試
                e.printStackTrace()
            }
        }
    }

    // 提供一個方法來重置狀態，例如在用戶開始輸入或離開屏幕時
    fun resetState() {
        _registrationState.value = RegistrationState.Idle
    }
}