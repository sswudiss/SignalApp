package com.example.signalapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.signalapp.ui.theme.SignalAppTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.signalapp.ui.registration.RegistrationScreen
import com.example.signalapp.navigation.AppNavigationHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SignalAppTheme { // 應用 Material 3 主題
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigationHost() // 呼叫我們自訂的導航主機 Composable
                }
            }
        }
    }
}


// --- 預覽 (可選，但推薦) ---
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SignalAppTheme {
        // 注意：直接預覽 AppNavigationHost 可能不會顯示特定頁面
        // 可以單獨預覽 RegistrationScreen 或 ChatListScreen
        RegistrationScreen(onRegistrationSuccess = {}) // 預覽註冊頁
        // ChatListScreen() // 或者預覽聊天列表頁
    }
}