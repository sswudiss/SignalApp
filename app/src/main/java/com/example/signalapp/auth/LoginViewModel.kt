package com.example.signalapp.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel : ViewModel() {

    private val auth: FirebaseAuth = Firebase.auth

    var username by mutableStateOf("")
        private set
    var password by mutableStateOf("")
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var loginSuccess by mutableStateOf(false)
        private set

    fun onUsernameChange(newUsername: String) {
        username = newUsername
        errorMessage = null
    }

    fun onPasswordChange(newPassword: String) {
        password = newPassword
        errorMessage = null
    }

    fun loginUser() {
        if (username.isBlank() || password.isBlank()) {
            errorMessage = "用戶名和密碼不能為空"
            return
        }

        errorMessage = null
        isLoading = true
        loginSuccess = false

        val email = "$username@cruise.com" // 同樣使用固定後綴

        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                isLoading = false
                loginSuccess = true // 登錄成功，觸發導航

            } catch (e: FirebaseAuthInvalidUserException) {
                isLoading = false
                errorMessage = "用戶不存在或已被禁用"
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                isLoading = false
                errorMessage = "密碼錯誤"
            } catch (e: Exception) {
                isLoading = false
                errorMessage = "登錄失敗: ${e.localizedMessage ?: "未知錯誤"}"
                // Log.e("LoginError", "Firebase login failed", e)
            }
        }
    }
}