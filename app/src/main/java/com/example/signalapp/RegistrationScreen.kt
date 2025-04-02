package com.example.signalapp


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

//注冊頁面

@Composable
fun RegistrationScreen(
    // 添加 ViewModel 參數，使用 viewModel() 獲取實例
    viewModel: RegistrationViewModel = viewModel(),
    onNavigateToHome: () -> Unit, // 添加導航回調
    onNavigateToLogin: () -> Unit // <--- 新增：導航到登錄頁的回調
) {

    val registrationState by viewModel.registrationState.collectAsStateWithLifecycle()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    val isLoading = registrationState is RegistrationState.Loading
    val firebaseError = if (registrationState is RegistrationState.Error) {
        (registrationState as RegistrationState.Error).message
    } else null
    val displayError = firebaseError ?: localError

    // 處理註冊成功的情況 (例如顯示消息或導航)
    LaunchedEffect(registrationState) {
        if (registrationState is RegistrationState.Success) {
            println("註冊成功! 準備導航...")
            // 調用傳入的導航 lambda
            onNavigateToHome()
            // 重置狀態避免重複導航或顯示成功狀態
            viewModel.resetState()
        }
        // 如果 Firebase 出錯，清除本地錯誤，因為 Firebase 錯誤優先級更高
        if (registrationState is RegistrationState.Error) {
            localError = null
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text("創建新的賬戶", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                localError = null // 清除本地錯誤
                viewModel.resetState() // 如果之前有 Firebase 錯誤，用戶輸入時重置狀態
            },
            label = { Text("用戶名") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            isError = localError?.contains("用戶名") == true, // 可選：更精確的錯誤高亮
            readOnly = isLoading // 註冊時禁止編輯
        )

        Spacer(modifier = Modifier.height(16.dp))

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
            isError = localError?.contains("密碼") == true || firebaseError?.contains("密碼") == true,
            readOnly = isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                localError = null
                viewModel.resetState()
            },
            label = { Text("確認密碼") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            isError = localError?.contains("密碼") == true, // 只檢查本地密碼匹配錯誤
            readOnly = isLoading
        )

        // 顯示錯誤信息
        if (displayError != null && !isLoading) { // 不在加載時顯示錯誤
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = displayError,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start)
            )
        } else {
            // 預留空間避免跳動
            Spacer(modifier = Modifier.height(8.dp + (MaterialTheme.typography.bodySmall.fontSize.value * 1.5).dp)) // 估算文本高度
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 註冊按鈕和加載指示器
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    // 執行本地驗證
                    if (username.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                        localError = "所有字段均為必填項。"
                    } else if (!username.all { it.isLetterOrDigit() || it == '_' }) { // 添加用戶名格式驗證 (示例)
                        localError = "用戶名只能包含字母、數字和下劃線。"
                    } else if (password != confirmPassword) {
                        localError = "兩次輸入的密碼不一致。"
                    } else if (password.length < 6) {
                        localError = "密碼至少需要 6 個字符。"
                    } else {
                        localError = null // 清除本地錯誤
                        // 調用 ViewModel 的註冊方法
                        viewModel.registerUser(username, password)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading // 加載時禁用按鈕
            ) {
                Text("註冊")
            }

            // 如果正在加載，顯示進度條
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.height(24.dp)) // 在按鈕和鏈接之間添加間距

            // --- 新增：已有賬戶？登錄鏈接 ---
            ClickableLoginText(onLoginClick = onNavigateToLogin)

            Spacer(modifier = Modifier.height(16.dp)) // 底部額外間距
        }
    }
}


// 將可點擊文本提取為獨立的 Composable (可選，但更清晰)
@Composable
private fun ClickableLoginText(onLoginClick: () -> Unit) {
    val annotatedText = buildAnnotatedString {
        append("已有賬戶？ ") // 靜態文本部分

        // "登錄" 部分
        pushStringAnnotation(tag = "LOGIN", annotation = "LOGIN") // 添加標籤以便識別點擊
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) { // 設置不同顏色
            append("登錄")
        }
        pop() // 結束標籤範圍
    }

    ClickableText(
        text = annotatedText,
        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp), // 設置基本樣式
        onClick = { offset ->
            // 檢查點擊的位置是否在 "LOGIN" 標籤範圍內
            annotatedText.getStringAnnotations(tag = "LOGIN", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    // 如果點擊了 "LOGIN" 部分，執行回調
                    if (annotation.item == "LOGIN") {
                        onLoginClick()
                    }
                }
        }
    )
}
