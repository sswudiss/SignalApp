package com.example.signalapp.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.signalapp.ui.theme.JJLLTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info // 導入 Info 圖標
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel


@Composable
fun RegistrationScreen(
    viewModel: RegistrationViewModel = viewModel(),
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val username = viewModel.username
    val password = viewModel.password
    val confirmPassword = viewModel.confirmPassword
    val errorMessage = viewModel.errorMessage
    val isLoading = viewModel.isLoading
    val registrationSuccess = viewModel.registrationSuccess

    val focusManager = LocalFocusManager.current

    LaunchedEffect(registrationSuccess) {
        if (registrationSuccess) {
            onRegisterSuccess()
        }
    }

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 32.dp)
                // 添加垂直滾動以防內容過多被鍵盤遮擋（特別是提示信息增加後）
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp)) // 稍微減少頂部空間

            Text("創建您的 JJLL 賬戶", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(16.dp)) // 在標題下方添加提示

            // --- 添加的提示信息 ---
            ConnectionInfoBox() // 使用封裝的 Composable

            Spacer(modifier = Modifier.height(24.dp)) // 提示信息和輸入框之間的間距

            // --- 用戶名、密碼、確認密碼輸入框 ... (保持不變) ---
            OutlinedTextField(
                value = username,
                onValueChange = { viewModel.onUsernameChange(it) },
                label = { Text("用戶名") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                isError = errorMessage?.contains("用戶名", ignoreCase = true) == true || // 匹配 "用戶名" 或 "username"
                        errorMessage?.contains("帳號只能包含", ignoreCase = true) == true || // 匹配新格式錯誤
                        (errorMessage != null && !errorMessage.contains("密碼", ignoreCase = true) && !errorMessage.contains("伺服器", ignoreCase = true) && !errorMessage.contains("網路", ignoreCase = true)), // 其他非密碼/網絡錯誤也可能與用戶名相關
                supportingText = { // 可以用 supportingText 顯示格式提示或錯誤
                    if (errorMessage?.contains("用戶名", ignoreCase = true) == true ||
                        errorMessage?.contains("帳號只能包含", ignoreCase = true) == true) {
                        Text(errorMessage)
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = { Text("密碼 (至少6位)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = errorMessage?.contains("密碼", ignoreCase = true) == true && errorMessage.contains("不一致", ignoreCase = true) != true, // 密碼相關且非不一致
                supportingText = {
                    if (errorMessage?.contains("密碼", ignoreCase = true) == true && errorMessage.contains("不一致", ignoreCase = true) != true) {
                        Text(errorMessage)
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { viewModel.onConfirmPasswordChange(it) },
                label = { Text("確認密碼") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = errorMessage?.contains("不一致", ignoreCase = true) == true, // 密碼不一致錯誤
                supportingText = {
                    if (errorMessage?.contains("不一致", ignoreCase = true) == true) {
                        Text(errorMessage)
                    }
                }
            )

            // --- 註冊按鈕和已有賬戶鏈接 ... (錯誤信息顯示調整) ---

            // 顯示通用或網絡相關的錯誤信息（非字段特定）
            val generalErrorMessage = errorMessage?.takeIf {
                !it.contains("用戶名", ignoreCase = true) &&
                        !it.contains("密碼", ignoreCase = true) &&
                        !it.contains("不一致", ignoreCase = true) &&
                        !it.contains("帳號只能包含", ignoreCase = true)
            }

            if (generalErrorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = generalErrorMessage, // 只顯示非字段特定的錯誤
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            } else {
                // 如果沒有其他錯誤，保留一些間距
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.registerUser()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("註冊")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ClickableText(
                text = AnnotatedString("已有賬戶？點此登錄"),
                onClick = { onNavigateToLogin() },
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(32.dp)) // 底部留些空間
        }
    }
}

// 封裝的提示信息框 Composable
@Composable
fun ConnectionInfoBox() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f), // 半透明背景
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = "提示信息",
            tint = MaterialTheme.colorScheme.onSecondaryContainer, // 圖標顏色
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "部分地區可能無法連接註冊服務器。如遇問題，請檢查網絡或稍後再試。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer, // 文字顏色
            fontWeight = FontWeight.Normal
        )
    }
}

// --- Preview ---
// (預覽代碼保持不變，但你可能想單獨預覽 ConnectionInfoBox)

@Preview(showBackground = true)
@Composable
fun ConnectionInfoBoxPreview() {
    JJLLTheme { // 或 JJLLTheme
        ConnectionInfoBox()
    }
}

@Preview(showBackground = true)
@Composable
fun RegistrationScreenWithInfoPreview() {
    JJLLTheme {
        RegistrationScreen(onRegisterSuccess = {}, onNavigateToLogin = {})
    }
}