package com.example.signalapp.auth

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.signalapp.shared.ConnectionViewModel
import com.example.signalapp.ui.theme.JJLLTheme

@Composable
fun RegistrationScreen(
    // ViewModel 實例，Compose 會自動處理其生命周期
    viewModel: RegistrationViewModel = viewModel(),
    registrationViewModel: RegistrationViewModel = viewModel(),

    // 獲取 Activity 範圍的 ConnectionViewModel
    connectionViewModel: ConnectionViewModel = viewModel(viewModelStoreOwner = LocalActivity.current as ComponentActivity),
    // 導航回調函數
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {

    // 從 ViewModel 獲取狀態
    val username = viewModel.username
    val password = viewModel.password
    val confirmPassword = viewModel.confirmPassword
    val errorMessage = viewModel.errorMessage
    val isLoading = viewModel.isLoading
    val registrationSuccess = viewModel.registrationSuccess
    // 用於管理焦點，例如隱藏鍵盤
    val focusManager = LocalFocusManager.current

    // ... 其他狀態獲取 ...
    val registrationErrorMessage = registrationViewModel.errorMessage // 獲取註冊本身的錯誤

    // 使用 LaunchedEffect 觀察註冊錯誤，並觸發全局錯誤提示
    LaunchedEffect(registrationErrorMessage) {
        if (registrationErrorMessage != null && registrationErrorMessage.contains("失敗")) { // 判斷是否是連接類錯誤
            // 如果註冊錯誤是連接失敗引起的（你需要判斷錯誤類型），顯示全局橫幅
            connectionViewModel.showConnectionError(registrationErrorMessage)
            // registrationViewModel.clearErrorMessage() // 可以考慮清除ViewModel自身的錯誤避免重複顯示
        }
    }

    // 監聽註冊成功狀態，成功後執行導航回調
    LaunchedEffect(registrationSuccess) {
        if (registrationSuccess) {
            onRegisterSuccess()
        }
    }

    // 使用 Scaffold 提供基本的 Material Design 佈局結構
    Scaffold { paddingValues ->
        // 使用 Column 將內容垂直排列，並推到頂部
        Column(
            modifier = Modifier
                .fillMaxSize() // 填滿整個屏幕
                .padding(paddingValues) // 應用 Scaffold 提供的內邊距
                .padding(horizontal = 32.dp), // 添加水平內邊距
            verticalArrangement = Arrangement.Top, // 內容置頂
            horizontalAlignment = Alignment.CenterHorizontally // 內容水平居中
        ) {
            // 在頂部留出一些空間，避免緊貼狀態欄
            Spacer(modifier = Modifier.height(60.dp))

            Text("創建您的 JJLL 賬戶", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(32.dp))

            // 用戶名輸入框
            OutlinedTextField(
                value = username,
                onValueChange = { viewModel.onUsernameChange(it) }, // 更新 ViewModel 中的狀態
                label = { Text("用戶名") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                isError = errorMessage?.contains("用戶名") == true // 如果錯誤信息包含“用戶名”，則標紅
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 密碼輸入框
            OutlinedTextField(
                value = password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = { Text("密碼 (至少6位)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(), // 隱藏密碼字符
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = errorMessage?.contains("密碼") == true && !errorMessage.contains("不一致") // 密碼相關錯誤（非不一致）
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 確認密碼輸入框
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { viewModel.onConfirmPasswordChange(it) },
                label = { Text("確認密碼") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = errorMessage?.contains("不一致") == true // 密碼不一致錯誤
            )

            // 顯示錯誤信息
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error, // 使用主題定義的錯誤顏色
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start) // 錯誤信息左對齊
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 註冊按鈕
            Button(
                onClick = {
                    focusManager.clearFocus() // 點擊按鈕時隱藏鍵盤
                    viewModel.registerUser() // 調用 ViewModel 的註冊方法
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading // 加載時禁用按鈕
            ) {
                // 根據加載狀態顯示文本或進度指示器
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary // 指示器顏色
                    )
                } else {
                    Text("註冊")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // "已有賬戶？登錄" 鏈接
            ClickableText(
                text = AnnotatedString("已有賬戶？點此登錄"),
                onClick = { offset ->
                    // 點擊文本時觸發導航
                    onNavigateToLogin()
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.primary // 使用主題的主顏色
                )
            )
        }
    }
}

// Composable 預覽
@Preview(showBackground = true)
@Composable
fun RegistrationScreenPreview() {
    JJLLTheme {
        RegistrationScreen(
            onRegisterSuccess = {}, // 預覽時不需要實際導航
            onNavigateToLogin = {}
        )
    }
}

// 預覽錯誤狀態
@Preview(showBackground = true)
@Composable
fun RegistrationScreenErrorPreview() {
    // 手動創建一個帶有錯誤信息的 ViewModel 進行預覽 (或者修改 ViewModel 允許預設狀態)
    val previewViewModel = RegistrationViewModel()
    // previewViewModel.errorMessage = "用戶名 'test' 已被註冊" // 這樣是無效的，因為屬性是 private set
    // 更好的預覽方式是直接在 Composable 中模擬狀態，或者讓 ViewModel 支持初始狀態注入

    JJLLTheme {
        // 這裡我們直接在 Composable 內部模擬狀態來預覽錯誤
        Scaffold { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))
                Text("創建您的 JJLL 賬戶", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(32.dp))
                OutlinedTextField(
                    value = "test",
                    onValueChange = {},
                    label = { Text("用戶名") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = true
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = "123456",
                    onValueChange = {},
                    label = { Text("密碼 (至少6位)") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = "123456",
                    onValueChange = {},
                    label = { Text("確認密碼") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "用戶名 'test' 已被註冊",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                    Text("註冊")
                }
                Spacer(modifier = Modifier.height(16.dp))
                ClickableText(
                    text = AnnotatedString("已有賬戶？點此登錄"),
                    onClick = {},
                    style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}