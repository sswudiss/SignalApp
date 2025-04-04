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
import android.util.Log // 導入 Log 用於調試
import com.google.firebase.FirebaseException // 可以捕獲更廣泛的 Firebase 錯誤
import com.google.firebase.auth.FirebaseAuthException // Auth 特有的錯誤基類
import java.io.IOException // 捕獲網絡 IO 錯誤

class RegistrationViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth

    var username by mutableStateOf("")
        private set
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

    fun onUsernameChange(newUsername: String) {
        username = newUsername
        errorMessage = null
    }

    fun onPasswordChange(newPassword: String) {
        password = newPassword
        errorMessage = null
    }

    fun onConfirmPasswordChange(newConfirmPassword: String) {
        confirmPassword = newConfirmPassword
        errorMessage = null
    }

    fun registerUser() {
        // 基本驗證
        if (username.isBlank()) {
            errorMessage = "用戶名不能為空"
            return
        }
        // Firebase 要求用戶名部分不能是純數字或其他非法字符，需要更嚴格的驗證，但這裡暫不處理
        if (!username.matches(Regex("^[a-zA-Z0-9._%+-]+$"))) { // 簡單驗證用戶名格式
            errorMessage = "用戶名只能包含字母、數字及 ._%+-"
            return
        }
        if (password.length < 6) {
            errorMessage = "密碼長度至少需要 6 位"
            return
        }
        if (password != confirmPassword) {
            errorMessage = "兩次輸入的密碼不一致"
            return
        }

        errorMessage = null
        isLoading = true
        registrationSuccess = false

        val email = "$username@cruise.com" // 使用固定後綴

        viewModelScope.launch {
            try {
                Log.d("Registration", "Attempting to register user: $email") // 添加日誌
                auth.createUserWithEmailAndPassword(email, password).await()
                Log.d("Registration", "Registration successful for: $email")
                isLoading = false
                registrationSuccess = true

            } catch (e: FirebaseAuthUserCollisionException) {
                // 用戶名（郵箱）已存在
                isLoading = false
                errorMessage = "用戶名 '$username' 已被註冊"
                Log.w("RegistrationError", "User collision for $email", e)

                // --- 捕獲網絡相關錯誤 ---
            } catch (e: IOException) {
                isLoading = false
                // 提示網絡連接問題
                errorMessage = "無法連接到註冊伺服器，請檢查您的網路連線或稍後重試。"
                Log.e("RegistrationError", "Network IO error during registration", e)

                // --- 捕獲其他 FirebaseAuth 錯誤 ---
            } catch (e: FirebaseAuthException) { // 捕獲其他 Firebase Auth 特定的錯誤，如密碼太弱等
                isLoading = false
                // 可以根據 e.errorCode 提供更詳細的錯誤信息，但通用消息通常足夠
                errorMessage = "註冊失敗: ${e.localizedMessage ?: "無效的輸入或伺服器問題"}"
                Log.e("RegistrationError", "FirebaseAuthException: ${e.errorCode}", e)

                // --- 捕獲其他 Firebase 平台級錯誤 ---
            } catch (e: FirebaseException) { // 捕獲更廣泛的 Firebase 錯誤 (如配置問題等)
                isLoading = false
                errorMessage = "註冊時發生 Firebase 服務錯誤，請稍後重試。"
                Log.e("RegistrationError", "FirebaseException", e)

                // --- 通用備份錯誤捕獲 ---
            } catch (e: Exception) {
                // 處理其他未知錯誤
                isLoading = false
                errorMessage = "註冊時發生未知錯誤: ${e.localizedMessage ?: "請稍後重試"}"
                Log.e("RegistrationError", "Generic Exception during registration", e)
            }
        }
    }
}