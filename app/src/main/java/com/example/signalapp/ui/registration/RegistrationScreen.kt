package com.example.signalapp.ui.registration

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.* // 導入 remember, mutableStateOf 等
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation // 用於密碼隱藏
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.signalapp.ui.theme.SignalAppTheme
import androidx.compose.runtime.LaunchedEffect         // 導入 LaunchedEffect
import androidx.compose.runtime.collectAsState          // 導入 collectAsState
import androidx.compose.runtime.getValue              // 導入 getValue 委託
import androidx.compose.material3.CircularProgressIndicator // 導入加載指示器
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun RegistrationScreen(
    onRegistrationSuccess: () -> Unit, // 保留回調，由 ViewModel 觸發時調用
    viewModel: RegistrationViewModel = hiltViewModel() // 不再需要工廠
) {
    val uiState by viewModel.uiState.collectAsState()

    // --- 使用 LaunchedEffect 處理副作用（導航） ---
    // 當 registrationSuccess 狀態變為 true 時，觸發一次 onRegistrationSuccess 回調
    LaunchedEffect(key1 = uiState.registrationSuccess) {
        if (uiState.registrationSuccess) {
            onRegistrationSuccess() // 調用外部傳入的導航函數
            viewModel.registrationNavigated() // 通知 ViewModel 導航已處理
        }
    }

    // --- UI 佈局 (大部分相同，但綁定到 ViewModel 的狀態和事件) ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            // --- 可選: 添加滾動以應對極端情況 ---
            // .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("註冊新帳戶", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(24.dp))

        // 用戶名輸入框 (綁定 ViewModel)
        OutlinedTextField(
            value = uiState.username,                // <--- 從 ViewModel 讀取
            onValueChange = { viewModel.updateUsername(it) }, // <--- 調用 ViewModel 方法
            label = { Text("用戶名") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = uiState.errorMessage?.contains("用戶名") == true // 可選: 錯誤時顯示紅色邊框
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 密碼輸入框 (綁定 ViewModel)
        OutlinedTextField(
            value = uiState.password,               // <--- 從 ViewModel 讀取
            onValueChange = { viewModel.updatePassword(it) },// <--- 調用 ViewModel 方法
            label = { Text("密碼") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            isError = uiState.errorMessage?.contains("密碼", ignoreCase = true) == true && !uiState.errorMessage!!.contains("兩次")
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 確認密碼輸入框 (綁定 ViewModel)
        OutlinedTextField(
            value = uiState.confirmPassword,         // <--- 從 ViewModel 讀取
            onValueChange = { viewModel.updateConfirmPassword(it) },// <--- 調用 ViewModel 方法
            label = { Text("確認密碼") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            isError = uiState.errorMessage?.contains("兩次", ignoreCase = true) == true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 顯示錯誤訊息 (來自 ViewModel)
        uiState.errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // 註冊按鈕 (觸發 ViewModel 處理)
        Button(
            // --- onClick 調用 ViewModel 的方法 ---
            onClick = { viewModel.attemptRegistration() },
            modifier = Modifier.fillMaxWidth(),
            // --- 按鈕在加載時應禁用 ---
            enabled = !uiState.isLoading
        ) {
            // --- 根據 isLoading 狀態顯示文本或進度條 ---
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary // 讓指示器在按鈕上可見
                )
            } else {
                Text("註冊")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        /*測試
        OutlinedButton( // 使用 OutlinedButton 與主按鈕區分
            onClick = {
                val username = uiState.username // 從狀態獲取
                val password = uiState.password // 從狀態獲取
                if (username.isNotBlank() && password.isNotBlank()) {
                    androidx.lifecycle.viewmodel.compose.viewModel.attemptLogin(username, password)
                } else {
                    // 可以提示用戶輸入用戶名和密碼
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("測試登錄 (使用上方輸入框)")
        }  */
    }
}


// 輔助預覽函數，展示靜態佈局
@Composable
private fun RegistrationScreenContentPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Text("註冊新帳戶", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(value = "user", onValueChange = {}, label = { Text("用戶名") }, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = "pass", onValueChange = {}, label = { Text("密碼") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = "pass", onValueChange = {}, label = { Text("確認密碼") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "示例錯誤信息",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("註冊") }
    }
}

// 預覽需要調整，因為它無法直接提供 ViewModel
// 可以創建一個假的 ViewModel 或傳遞假的狀態用於預覽
@Preview(showBackground = true)
@Composable
fun RegistrationScreenPreview() {
    SignalAppTheme {
        // 注意：直接預覽需要 ViewModel 的 Screen 會比較麻煩
        // 一種方法是修改 Screen 接受 UiState 作為參數，而不是 ViewModel
        // 另一種是預覽時不調用實際需要 ViewModel 的部分
        Surface(modifier = Modifier.fillMaxSize()) { // 加個 Surface 背景
            // 預覽時我們無法真正 "注入" ViewModel，所以預覽功能會受限
            // 這裡只預覽靜態佈局
            RegistrationScreenContentPreview() // 創建一個不依賴ViewModel的預覽輔助函數
        }
    }
}