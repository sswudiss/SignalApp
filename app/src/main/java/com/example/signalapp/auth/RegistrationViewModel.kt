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
import com.google.firebase.firestore.ktx.firestore


// 這個 ViewModel 不需要 Hilt，因為它直接訪問靜態的 Firebase 實例
// 如果未來需要注入 Repository，則需要添加 @HiltViewModel 和 @Inject
class RegistrationViewModel : ViewModel() {

    // Firebase Auth 實例
    private val auth: FirebaseAuth = Firebase.auth
    // Firestore 實例
    private val firestore = Firebase.firestore

    // --- UI 狀態 ---
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

    // --- 狀態更新函數 ---
    fun onUsernameChange(newUsername: String) {
        username = newUsername
        // 在用戶輸入時清除錯誤，提供即時反饋
        if (errorMessage?.contains("用戶名", ignoreCase = true) == true ||
            errorMessage?.contains("帳號只能包含", ignoreCase = true) == true) {
            errorMessage = null
        }
    }

    fun onPasswordChange(newPassword: String) {
        password = newPassword
        if (errorMessage?.contains("密碼", ignoreCase = true) == true ||
            errorMessage?.contains("6位", ignoreCase = true) == true) {
            errorMessage = null
        }
        // 同時清除 "不一致" 的錯誤，因為可能由這次修改觸發
        if(errorMessage?.contains("不一致", ignoreCase = true) == true) {
            errorMessage = null
        }
    }

    fun onConfirmPasswordChange(newConfirmPassword: String) {
        confirmPassword = newConfirmPassword
        if (errorMessage?.contains("不一致", ignoreCase = true) == true) {
            errorMessage = null
        }
    }

    // --- 註冊核心邏輯 ---
    fun registerUser() {
        // 基本驗證
        if (username.isBlank()) {
            errorMessage = "用戶名不能為空"
            return
        }
        // Firestore 限制 document ID (uid) 不能是 "." 或 ".."，也不能包含 "/"
        // 用戶名 username 允許更多字符，但我們在這裡也做些基本限制
        // 允許字母、數字、下劃線、點、百分號、加減號
        val usernamePattern = "^[a-zA-Z0-9._%+-]+$"
        val cleanUsername = username.trim() // 去除前後空格
        if (!cleanUsername.matches(Regex(usernamePattern))) {
            errorMessage = "用戶名只能包含字母、數字及 ._%+-" // 保持一致的提示
            return
        }
        if (password.isBlank()) { // 添加密碼為空判斷
            errorMessage = "密碼不能為空"
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

        // 清空舊錯誤，設置加載狀態
        errorMessage = null
        isLoading = true
        registrationSuccess = false

        // 構造用於 Auth 的 Email
        val email = "$cleanUsername@cruise.com"

        viewModelScope.launch {
            try {
                // --- 1. 創建 Firebase Auth 用戶 ---
                Log.d("Registration", "Attempting to create Auth user: $email")
                // 使用用戶輸入的密碼 (password)，而不是 username
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val newUser = authResult.user
                Log.d("Registration", "Auth user created successfully: ${newUser?.uid}")

                if (newUser != null) {
                    // --- 2. 在 Firestore 中創建用戶文檔 ---
                    val userUid = newUser.uid
                    // 準備寫入 Firestore 的數據
                    val userData = hashMapOf(
                        "username" to cleanUsername,       // 用戶註冊時的唯一用戶名
                        "displayName" to cleanUsername,    // 初始顯示名與用戶名相同
                        "photoUrl" to null as String?,      // 明確設置初始頭像為 null
                        "createdAt" to System.currentTimeMillis() // 添加創建時間戳
                    )

                    Log.d("Registration", "Attempting to create Firestore document for UID: $userUid with data: $userData")
                    firestore.collection("users").document(userUid)
                        .set(userData) // 使用 set 進行覆蓋寫入或創建
                        .await()       // 等待寫入操作完成

                    Log.d("Registration", "Firestore user document created successfully for UID: $userUid")

                    // 只有 Auth 和 Firestore 都成功才標記為成功
                    isLoading = false
                    registrationSuccess = true // 觸發成功導航

                } else {
                    // 理論上 createUser 不會返回 null User，但以防萬一
                    throw IllegalStateException("Firebase Auth user is null after successful creation.")
                }

            } catch (e: FirebaseAuthUserCollisionException) {
                // 用戶名（郵箱）衝突
                isLoading = false
                errorMessage = "用戶名 '$cleanUsername' 已被註冊"
                Log.w("RegistrationError", "User collision for email $email (username: $cleanUsername)", e)

            } catch (e: IOException) {
                // 網絡連接錯誤
                isLoading = false
                errorMessage = "無法連接到伺服器，請檢查您的網路連線或稍後重試。"
                Log.e("RegistrationError", "Network IO error during registration", e)

            } catch (e: FirebaseAuthException) {
                // 其他 Firebase Auth 驗證錯誤 (例如密碼太弱、郵箱格式無效等)
                isLoading = false
                errorMessage = when (e.errorCode) { // 可以根據錯誤碼給出更具體的提示
                    "ERROR_WEAK_PASSWORD" -> "密碼強度不足，請使用更複雜的密碼"
                    "ERROR_INVALID_EMAIL" -> "用戶名格式無效，請檢查" // 雖然我們後綴了，但前面部分可能有問題
                    else -> "註冊 Auth 時發生錯誤: ${e.localizedMessage ?: "請重試"}"
                }
                Log.e("RegistrationError", "FirebaseAuthException: ${e.errorCode}", e)

            } catch (e: FirebaseException) {
                // 其他 Firebase 平台級錯誤 (例如 Firestore 寫入錯誤，但未被 FirebaseAuthException 捕獲)
                isLoading = false
                errorMessage = "與 Firebase 服務通信時出錯，請稍後重試。"
                Log.e("RegistrationError", "FirebaseException (possibly Firestore error)", e)

            } catch (e: Exception) {
                // 通用備份錯誤捕獲
                isLoading = false
                errorMessage = "註冊過程中發生未知錯誤: ${e.localizedMessage ?: "請稍後重試"}"
                Log.e("RegistrationError", "Generic Exception during registration or Firestore save", e)
                // 這裡可以考慮更細緻的錯誤處理，例如如果 Auth 成功但 Firestore 失敗，
                // 理論上應該嘗試刪除剛創建的 Auth 用戶以保持一致性，但這會增加複雜性。
            }
        }
    }
}