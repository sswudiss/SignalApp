package com.example.signalapp.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class RegistrationViewModel : ViewModel() {

    // 使用 Firebase Auth 實例
    private val auth: FirebaseAuth = Firebase.auth

    // UI 狀態，使用 Compose 的 State Delegate
    var username by mutableStateOf("")
        private set // 外部只能讀取

    var password by mutableStateOf("")
        private set

    var confirmPassword by mutableStateOf("")
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var registrationSuccess by mutableStateOf(false)
        private set

    // 更新用戶名狀態
    fun onUsernameChange(newUsername: String) {
        username = newUsername
        errorMessage = null // 清除之前的錯誤信息
    }

    // 更新密碼狀態
    fun onPasswordChange(newPassword: String) {
        password = newPassword
        errorMessage = null
    }

    // 更新確認密碼狀態
    fun onConfirmPasswordChange(newConfirmPassword: String) {
        confirmPassword = newConfirmPassword
        errorMessage = null
    }

    // 執行註冊邏輯
    fun registerUser() {
        // 基本驗證
        if (username.isBlank()) {
            errorMessage = "用戶名不能為空"
            return
        }
        if (password.length < 6) { // Firebase 密碼至少需要 6 位
            errorMessage = "密碼長度至少需要 6 位"
            return
        }
        if (password != confirmPassword) {
            errorMessage = "兩次輸入的密碼不一致"
            return
        }

        // 清除之前的錯誤並顯示加載狀態
        errorMessage = null
        isLoading = true
        registrationSuccess = false // 重置成功狀態

        // 構造符合要求的郵箱地址
        val email = "$username@cruise.com" // 使用固定後綴

        // 使用 viewModelScope 啟動協程執行異步操作
        viewModelScope.launch {
            try {
                // 調用 Firebase Auth API 創建用戶
                auth.createUserWithEmailAndPassword(email, password).await() // await() 將 Task 轉換為 suspend 函數

                // 註冊成功
                isLoading = false
                registrationSuccess = true // 設置成功標誌，觸發導航

            } catch (e: FirebaseAuthUserCollisionException) {
                // 處理用戶名（郵箱）已存在的情況
                isLoading = false
                errorMessage = "用戶名 '$username' 已被註冊"
            } catch (e: Exception) {
                // 處理其他可能的 Firebase 錯誤或網絡錯誤
                isLoading = false
                errorMessage = "註冊失敗: ${e.localizedMessage ?: "未知錯誤"}"
                // Log.e("RegistrationError", "Firebase registration failed", e) // 可以在 Logcat 中記錄詳細錯誤
            }
        }
    }
}