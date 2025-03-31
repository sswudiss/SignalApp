package your_package_name.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.* // 保持此導入
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel // 導入 viewModel()
import com.example.signalapp.viewmodel.AuthViewModel

@Composable
fun RegistrationScreen(
    authViewModel: AuthViewModel = viewModel(), // 獲取 ViewModel 實例
    onNavigateToLogin: () -> Unit,
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
        Text("創建帳號", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        // --- 用戶名 ---
        OutlinedTextField(
            value = uiState.username, // 從 uiState 讀取
            onValueChange = { authViewModel.updateUsername(it) }, // 調用 ViewModel 更新
            label = { Text("用戶名") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = uiState.errorMessage?.contains("用戶名") == true // 簡單標記錯誤欄位
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
            isError = uiState.errorMessage?.contains("密碼") == true && !uiState.errorMessage!!.contains("用戶名") // 避免同時標紅
        )
        Spacer(modifier = Modifier.height(8.dp))

        // --- 確認密碼 ---
        OutlinedTextField(
            value = uiState.confirmPassword, // 從 uiState 讀取
            onValueChange = { authViewModel.updateConfirmPassword(it) }, // 調用 ViewModel 更新
            label = { Text("確認密碼") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            // 也可以根據 uiState.errorMessage 判斷 isError
            isError = uiState.errorMessage?.contains("密碼不一致") == true
        )
        Spacer(modifier = Modifier.height(16.dp))

        // --- 重要警告 ---
        if (uiState.showRegistrationWarning) { // 可以由 ViewModel 控制是否顯示
            Text(
                text = "重要提示：請務必牢記您的用戶名和密碼。一旦忘記，將無法找回或重設帳號。",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // --- 錯誤訊息 ---
        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage!!, // 從 uiState 讀取
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // --- 註冊按鈕 ---
        Button(
            onClick = { authViewModel.registerUser() }, // 調用 ViewModel 的註冊函數
            enabled = !uiState.isLoading, // 從 uiState 讀取
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isLoading) { // 從 uiState 讀取
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("註冊")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // --- 切換到登入 ---
        TextButton(
            onClick = onNavigateToLogin, // 使用傳入的回調
            enabled = !uiState.isLoading // 加載時也可禁用切換 (可選)
        ) {
            Text("已有帳號？ 前往登入")
        }
    }
}

// Preview 維持不變或稍微調整以模擬不同狀態
@Preview(showBackground = true)
@Composable
fun RegistrationScreenPreview() {
    // 注意：預覽無法真正獲取 ViewModel，所以這裡的互動是無效的
    // 可以創建一個假的 ViewModel 或直接預覽靜態狀態
    val previewViewModel = AuthViewModel() // 僅供預覽結構
    RegistrationScreen(
        authViewModel = previewViewModel,
        onNavigateToLogin = {},
        onNavigateToMainApp = {}
    )
    // 你可以手動設置 previewViewModel 的狀態來預覽不同情況，但這比較麻煩
}