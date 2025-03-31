package com.example.signalapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth


@Composable
fun MainAppScreen(
    authViewModel: com.example.signalapp.viewmodel.AuthViewModel, // 傳入 AuthViewModel 以便登出
    onLoggedOut: () -> Unit // 登出後的回調，用於觸發導航
) {
    val currentUser = FirebaseAuth.getInstance().currentUser // 獲取當前用戶信息 (可選顯示)
    val username = currentUser?.email?.substringBefore('@') ?: "未知用戶" // 從假 email 中提取用戶名

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("歡迎！ $username") // 顯示用戶名
        Spacer(modifier = Modifier.height(32.dp))

        // 在這裡放置你的仿 Signal 主要內容...
        Text("這裡是主應用程式介面")
        Spacer(modifier = Modifier.height(32.dp))


        Button(onClick = {
            authViewModel.logoutUser() // 調用 ViewModel 登出
            // 注意：實際的導航將由 AuthStateListener 處理
            // onLoggedOut() // 這裡不再需要手動調用導航，監聽器會做
        }) {
            Text("登出")
        }
    }
}