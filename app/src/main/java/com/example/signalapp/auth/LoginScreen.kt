package com.example.signalapp.auth

import androidx.activity.ComponentActivity // 導入 ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions // 導入 KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // 導入 LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction // 導入 ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.signalapp.shared.ConnectionViewModel
import com.example.signalapp.ui.theme.JJLLTheme
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import java.io.IOException // 假設網絡錯誤可能拋出 IOException 或類似錯誤

@Composable
fun LoginScreen(
    // ViewModel 注入:
    loginViewModel: LoginViewModel = viewModel(), // 登錄頁自己的 ViewModel
    connectionViewModel: ConnectionViewModel, // = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity) // 從 AppNavigation 傳入，或在此處獲取
    // 導航回調:
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    // --- State ---
    val username = loginViewModel.username
    val password = loginViewModel.password
    val localErrorMessage = loginViewModel.errorMessage // **本地錯誤信息** (如密碼錯誤)
    val isLoading = loginViewModel.isLoading
    val loginSuccess = loginViewModel.loginSuccess

    val focusManager = LocalFocusManager.current

    // --- Effects ---
    // 觀察登錄成功狀態，觸發導航
    LaunchedEffect(loginSuccess) {
        if (loginSuccess) {
            onLoginSuccess()
        }
    }

    // **觀察本地錯誤信息，判斷是否需要顯示全局連接錯誤橫幅**
    LaunchedEffect(localErrorMessage) {
        if (localErrorMessage != null) {
            // 根據錯誤消息內容判斷是否是連接錯誤 (這是一種簡化判斷)
            // 更健壯的方式是在 ViewModel 中區分錯誤類型
            if (localErrorMessage.contains("失敗") || localErrorMessage.contains("network") || localErrorMessage.contains("timeout")) {
                connectionViewModel.showConnectionError(localErrorMessage)
                // loginViewModel.clearErrorMessage() // 可選：清除本地錯誤，避免同時顯示兩種提示
            }
        }
    }

    // --- UI ---
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 32.dp)
                // 添加點擊空白處隱藏鍵盤的功能 (如果需要的話)
                // .clickable( // 不推薦直接在 Column 上加，可能影響子組件點擊
                //    interactionSource = remember { MutableInteractionSource() },
                //    indication = null // 無點擊效果
                // ) { focusManager.clearFocus() },
                .safeContentPadding(), // 更好的方式是使用 safe drawing/content padding
            verticalArrangement = Arrangement.Top, // 保持置頂
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp)) // 頂部空間

            Text("歡迎回來！", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(32.dp))

            // 用戶名輸入框
            OutlinedTextField(
                value = username,
                onValueChange = loginViewModel::onUsernameChange, // 使用方法引用
                label = { Text("用戶名") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next // 鍵盤下一項
                ),
                isError = localErrorMessage != null // 如果有任何本地錯誤，可能與用戶名有關
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 密碼輸入框
            OutlinedTextField(
                value = password,
                onValueChange = loginViewModel::onPasswordChange,
                label = { Text("密碼") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done // 鍵盤完成按鈕
                ),
                keyboardActions = KeyboardActions( // 添加鍵盤動作
                    onDone = {
                        focusManager.clearFocus() // 隱藏鍵盤
                        if (!isLoading && username.isNotBlank() && password.isNotBlank()) {
                            loginViewModel.loginUser() // 觸發登錄
                        }
                    }
                ),
                isError = localErrorMessage?.contains("密碼") == true || localErrorMessage?.contains("錯誤") == true // 標記特定錯誤
            )

            // --- **顯示本地錯誤信息** ---
            AnimatedVisibility(visible = localErrorMessage != null && !localErrorMessage.contains("失敗")) { // 只顯示非連接類的本地錯誤
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = localErrorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start) // 左對齊
                )
            }


            Spacer(modifier = Modifier.height(24.dp))

            // 登錄按鈕
            Button(
                onClick = {
                    focusManager.clearFocus()
                    loginViewModel.loginUser()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && username.isNotBlank() && password.isNotBlank() // 添加非空判斷
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary // 指示器顏色
                    )
                } else {
                    Text("登錄")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // "還沒有賬戶？註冊" 鏈接
            ClickableText(
                text = AnnotatedString("還沒有賬戶？點此註冊"),
                onClick = { offset ->
                    // 只有在非加載狀態下才能點擊跳轉
                    if (!isLoading) {
                        onNavigateToRegister()
                    }
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.primary // 主題顏色
                )
            )
        }
    }
}

// --- 修改 LoginViewModel 以更好地區分錯誤 (可選但推薦) ---
/*
// 在 LoginViewModel.kt 內部
class LoginViewModel(...) {
    // ... state ...

    // 可選: 添加一個專門的連接錯誤狀態，或者讓 showConnectionError 返回 Boolean
    // var isConnectionError by mutableStateOf(false)
    //    private set

    fun loginUser() {
        // ... 驗證 ...
        errorMessage = null
        // isConnectionError = false
        isLoading = true
        loginSuccess = false
        val email = "$username@cruise.com"

        viewModelScope.launch {
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                isLoading = false
                loginSuccess = true

            } catch (e: FirebaseAuthInvalidUserException) {
                isLoading = false
                errorMessage = "用戶不存在或已被禁用" // 本地錯誤
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                isLoading = false
                errorMessage = "密碼錯誤" // 本地錯誤
            } catch (e: IOException) { // 捕獲網絡相關錯誤
                isLoading = false
                errorMessage = "網絡連接失敗，請檢查網絡後重試" // 設置本地錯誤，Compose 會觸發全局顯示
                // isConnectionError = true // 設置標誌位
            }
            catch (e: Exception) { // 其他 Firebase 或未知錯誤
                isLoading = false
                // 判斷其他可能代表連接問題的 Firebase 異常
                if (e.message?.contains("network error") == true || e.message?.contains("TIMEOUT") == true) {
                     errorMessage = "連接服務器超時或發生網絡錯誤"
                     // isConnectionError = true
                } else {
                     errorMessage = "登錄失敗: ${e.localizedMessage ?: "未知錯誤"}" // 其他本地錯誤
                }
            }
        }
    }
     // 添加清除錯誤的方法
     // fun clearErrorMessage() { errorMessage = null }
}
*/


// --- Preview ---
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    JJLLTheme {
        // 預覽需要一個 ConnectionViewModel 實例
        val previewConnectionViewModel: ConnectionViewModel = viewModel() // 預覽時創建臨時實例
        LoginScreen(
            connectionViewModel = previewConnectionViewModel,
            onLoginSuccess = {},
            onNavigateToRegister = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenLoadingPreview() {
    JJLLTheme {
        val previewConnectionViewModel: ConnectionViewModel = viewModel()
        // 如何在預覽中設置 ViewModel 狀態？
        // 1. 修改 ViewModel 允許外部設置初始狀態（用於測試/預覽）
        // 2. 創建一個假的 ViewModel 實現
        // 3. 直接在 Composable 預覽中模擬狀態（如下）

        // 模擬加載狀態
        Scaffold { pv ->
            Column(
                modifier = Modifier.fillMaxSize().padding(pv).padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))
                Text("歡迎回來！", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(32.dp))
                OutlinedTextField(value = "user", onValueChange = {}, label = { Text("用戶名") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = "password", onValueChange = {}, label = { Text("密碼") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = {}, modifier = Modifier.fillMaxWidth(), enabled = false) { // enabled = false
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                }
                Spacer(modifier = Modifier.height(16.dp))
                ClickableText(text = AnnotatedString("還沒有賬戶？點此註冊"), onClick = {})
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun LoginScreenLocalErrorPreview() {
    JJLLTheme {
        val previewConnectionViewModel: ConnectionViewModel = viewModel()
        // 模擬本地錯誤狀態
        Scaffold { pv ->
            Column(
                modifier = Modifier.fillMaxSize().padding(pv).padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))
                Text("歡迎回來！", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(32.dp))
                OutlinedTextField(value = "user", onValueChange = {}, label = { Text("用戶名") }, modifier = Modifier.fillMaxWidth(), isError=true) // isError = true
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = "wrongpass", onValueChange = {}, label = { Text("密碼") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation(), isError=true) // isError = true
                Spacer(modifier = Modifier.height(8.dp))
                // 顯示模擬的本地錯誤信息
                Text(
                    text = "密碼錯誤", // 模擬的錯誤消息
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(16.dp)) // 調整間距以適應錯誤文本
                Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                    Text("登錄")
                }
                Spacer(modifier = Modifier.height(16.dp))
                ClickableText(text = AnnotatedString("還沒有賬戶？點此註冊"), onClick = {})
            }
        }
    }
}