package com.example.signalapp.screens


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// 定義登錄狀態
sealed class LoginState {
    object Idle : LoginState() // 初始狀態
    object Loading : LoginState() // 正在登錄
    object Success : LoginState() // 登錄成功
    data class Error(val message: String) : LoginState() // 登錄失敗
}

class LoginViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun loginUser(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("用戶名和密碼不能為空。")
            return
        }

        _loginState.value = LoginState.Loading
        val email = "$username@cruise.com"

        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                // 登錄成功
                _loginState.value = LoginState.Success
            } catch (e: FirebaseAuthInvalidUserException) {
                // 用戶不存在或被禁用/刪除
                _loginState.value = LoginState.Error("用戶名 '$username' 不存在或賬戶已被禁用。")
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                // 密碼錯誤
                _loginState.value = LoginState.Error("密碼錯誤，請重試。")
            } catch (e: Exception) {
                // 其他 Firebase 或網絡錯誤
                _loginState.value = LoginState.Error("登錄失敗：${e.localizedMessage ?: "未知錯誤"}")
                e.printStackTrace() // 打印詳細錯誤以供調試
            }
        }
    }

    // 重置狀態
    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}