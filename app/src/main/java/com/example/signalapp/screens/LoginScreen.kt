package com.example.signalapp.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.rememberScrollState

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(), // 獲取 LoginViewModel 實例
    onLoginSuccess: () -> Unit, // 登錄成功的回調
    onNavigateToRegister: () -> Unit // 導航到註冊頁的回調
) {
    // 從 ViewModel 收集狀態
    val loginState by viewModel.loginState.collectAsStateWithLifecycle()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) } // 本地驗證錯誤

    // 從 ViewModel 提取 Firebase 錯誤信息
    val firebaseError = if (loginState is LoginState.Error) {
        (loginState as LoginState.Error).message
    } else {
        null
    }

    // 顯示錯誤信息 (Firebase 優先)
    val displayError = firebaseError ?: localError
    val isLoading = loginState is LoginState.Loading

    // 監聽登錄成功狀態以觸發導航
    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            println("登錄成功! 準備導航...")
            onLoginSuccess()
            // 可以在成功後重置狀態，雖然導航後此屏幕實例可能銷毀
            viewModel.resetState()
        }
        // 如果有 Firebase 錯誤，清除本地錯誤
        if (loginState is LoginState.Error) {
            localError = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // 添加滾動
            .padding(16.dp),
        verticalArrangement = Arrangement.Top, // 與註冊頁對齊方式一致
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Text("登錄你的賬戶", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(32.dp))

        // --- 用戶名輸入框 ---
        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                localError = null // 清除本地錯誤
                viewModel.resetState() // 輸入時重置 ViewModel 狀態
            },
            label = { Text("用戶名") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            readOnly = isLoading, // 加載時只讀
            isError = displayError?.contains("用戶名", ignoreCase = true) == true // 標記錯誤
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- 密碼輸入框 ---
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                localError = null
                viewModel.resetState()
            },
            label = { Text("密碼") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            readOnly = isLoading,
            isError = displayError?.contains("密碼", ignoreCase = true) == true // 標記錯誤
        )

        // --- 顯示錯誤信息 ---
        if (displayError != null && !isLoading) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = displayError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start)
            )
        } else {
            // 預留空間避免跳動
            Spacer(modifier = Modifier.height(8.dp + (MaterialTheme.typography.bodySmall.fontSize.value * 1.5).dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 登錄按鈕和加載指示器 ---
        Box(contentAlignment = Alignment.Center) {
            Button(
                onClick = {
                    // 本地簡單驗證
                    if (username.isBlank() || password.isBlank()) {
                        localError = "用戶名和密碼不能為空。"
                    } else {
                        localError = null
                        // 調用 ViewModel 登錄
                        viewModel.loginUser(username, password)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading // 加載時禁用
            ) {
                Text("登錄")
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 新增：還沒有賬戶？註冊鏈接 ---
        ClickableRegisterText(onRegisterClick = onNavigateToRegister)

        Spacer(modifier = Modifier.height(16.dp)) // 底部額外間距
    }
}

// "註冊" 可點擊文本 Composable
@Composable
private fun ClickableRegisterText(onRegisterClick: () -> Unit) {
    val annotatedText = buildAnnotatedString {
        append("還沒有賬戶？ ")
        pushStringAnnotation(tag = "REGISTER", annotation = "REGISTER")
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
            append("註冊")
        }
        pop()
    }

    ClickableText(
        text = annotatedText,
        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
        onClick = { offset ->
            annotatedText.getStringAnnotations(tag = "REGISTER", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    if (annotation.item == "REGISTER") {
                        onRegisterClick()
                    }
                }
        }
    )
}