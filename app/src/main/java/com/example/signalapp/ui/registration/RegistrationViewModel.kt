package com.example.signalapp.ui.registration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.signalapp.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor( // <--- 使用 @Inject 注入依賴
    private val userRepository: UserRepository // 直接聲明 UserRepository 依賴
) : ViewModel() { // <--- 改回繼承標準 ViewModel

    private val _uiState = MutableStateFlow(RegistrationUiState())
    val uiState: StateFlow<RegistrationUiState> = _uiState.asStateFlow()

    // --- 事件處理函數 ---

    fun updateUsername(username: String) {
        _uiState.update { currentState ->
            currentState.copy(username = username, errorMessage = null) // 輸入時清除錯誤
        }
    }

    fun updatePassword(password: String) {
        _uiState.update { currentState ->
            currentState.copy(password = password, errorMessage = null)
        }
    }

    fun updateConfirmPassword(confirmPassword: String) {
        _uiState.update { currentState ->
            currentState.copy(confirmPassword = confirmPassword, errorMessage = null)
        }
    }
    fun attemptRegistration() {
        if (_uiState.value.isLoading) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        val currentState = _uiState.value

        // --- 前端基本驗證 (非空、密碼匹配) ---
        val validationError = validateInputs(currentState)
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError, isLoading = false) }
            return
        }

        // --- 調用 Repository 進行註冊 ---
        viewModelScope.launch {
            // 1. 檢查用戶名是否已存在 (通過 Repository)
            val isTaken = userRepository.isUsernameTaken(currentState.username)
            if (isTaken) {
                _uiState.update {
                    it.copy(errorMessage = "用戶名 '${currentState.username}' 已被註冊", isLoading = false)
                }
                return@launch // 結束協程
            }

            // 2. 執行註冊 (如果上面檢查通過，這裡理論上 registerUser 內部檢查會多餘，但保留無妨)
            val registrationSuccess = userRepository.registerUser(currentState.username, currentState.password)
            if (registrationSuccess) {
                println("用戶 ${currentState.username} 註冊成功！(數據庫)")
                // 更新 UI 狀態為成功
                _uiState.update {
                    it.copy(isLoading = false, registrationSuccess = true)
                }
            } else {
                // 處理註冊失敗（可能是併發衝突或其他 Repository/DB 錯誤）
                println("註冊失敗: Repository 返回 false")
                _uiState.update {
                    it.copy(errorMessage = "註冊過程中發生未知錯誤", isLoading = false)
                }
            }
        }
    }

    // 輔助驗證函數
    private fun validateInputs(state: RegistrationUiState): String? {
        return when {
            state.username.isBlank() -> "用戶名不能為空"
            state.password.isBlank() -> "密碼不能為空"
            state.password.length < 6 -> "密碼長度不能少於 6 位" // 添加密碼複雜度要求示例
            state.password != state.confirmPassword -> "兩次輸入的密碼不一致"
            else -> null // 驗證通過
        }
    }

    fun registrationNavigated() {
        _uiState.update { it.copy(registrationSuccess = false) }
    }

    //測試
    //添加登錄邏輯（暫時在註冊頁模擬）: 為了測試 validateUser，我們需要一個地方輸入用戶名和密碼來嘗試驗證。
    // 一個快速的方法是在 RegistrationViewModel 中添加一個臨時的 "登錄" 函數，
    // 並在 RegistrationScreen 上添加一個臨時按鈕觸發它（或者直接在 attemptRegistration 後嘗試登錄）。
    fun attemptLogin(username: String, password: String) {
        viewModelScope.launch {
            val isValid = userRepository.validateUser(username, password)
            if (isValid) {
                println("登錄成功: 用戶 $username")
                // TODO: 實際登錄成功後的操作，例如導航到 ChatList
                // 為了測試，我們可以暫時也在這裡觸發導航
                _uiState.update { it.copy(isLoading = false, registrationSuccess = true) } // 復用成功標誌
            } else {
                println("登錄失敗: 用戶名或密碼錯誤 for $username")
                _uiState.update { it.copy(errorMessage = "用戶名或密碼錯誤", isLoading = false) }
            }
        }
    }//測試
}
