package com.example.signalapp.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.signalapp.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = viewModel(), // 獲取 ViewModel 實例
    onNavigateToRegister: () -> Unit,
    onNavigateToMainApp: () -> Unit // 新增: 導航到主應用的回調
) {
    // 從 ViewModel 收集 UI 狀態
    val uiState by authViewModel.uiState.collectAsState()

    // 使用 LaunchedEffect 監聽導航觸發
    LaunchedEffect(key1 = uiState.navigateToMainApp) {
        if (uiState.navigateToMainApp) {
            onNavigateToMainApp() // 執行實際的導航操作
            authViewModel.navigationCompleted() // 通知 ViewModel 導航已處理
        }
    }

    // 使用 LaunchedEffect 在 Composable 進入組合時清除之前的錯誤訊息 (可選)
    LaunchedEffect(key1 = Unit) {
        authViewModel.clearErrorMessage()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("登入", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        // --- 用戶名 ---
        OutlinedTextField(
            value = uiState.username, // 從 uiState 讀取
            onValueChange = { authViewModel.updateUsername(it) }, // 調用 ViewModel 更新
            label = { Text("用戶名") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = uiState.errorMessage != null // 如果有任何錯誤，標紅輸入框
        )
        Spacer(modifier = Modifier.height(8.dp))

        // --- 密碼 ---
        OutlinedTextField(
            value = uiState.password, // 從 uiState 讀取
            onValueChange = { authViewModel.updatePassword(it) }, // 調用 ViewModel 更新
            label = { Text("密碼") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            isError = uiState.errorMessage != null // 如果有任何錯誤，標紅輸入框
        )
        Spacer(modifier = Modifier.height(16.dp))

        // --- 錯誤訊息 ---
        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage!!, // 從 uiState 讀取
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // --- 登入按鈕 ---
        Button(
            onClick = { authViewModel.loginUser() }, // 調用 ViewModel 的登入函數
            enabled = !uiState.isLoading, // 從 uiState 讀取
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isLoading) { // 從 uiState 讀取
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("登入")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // --- 切換到註冊 ---
        TextButton(
            onClick = onNavigateToRegister, // 使用傳入的回調
            enabled = !uiState.isLoading // 加載時也可禁用切換 (可選)
        ) {
            Text("還沒有帳號？ 前往註冊")
        }
    }
}

// Preview 維持不變或稍微調整
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    val previewViewModel = AuthViewModel() // 僅供預覽結構
    LoginScreen(
        authViewModel = previewViewModel,
        onNavigateToRegister = {},
        onNavigateToMainApp = {}
    )
}