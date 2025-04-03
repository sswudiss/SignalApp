package com.example.signalapp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.signalapp.navigation.AppNavigation
import com.example.signalapp.navigation.NavigationDestination
import com.example.signalapp.ui.theme.JJLLTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth // 初始化 Firebase Auth

        setContent {
            JJLLTheme { // 應用我們定義的主題
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 檢查登錄狀態並設置初始路由
                    var startDestination by remember { mutableStateOf<String?>(null) }

                    LaunchedEffect(key1 = Unit) { // LaunchedEffect 只在 Composable 首次進入組合時執行一次（或 key 變化時）
                        val currentUser = auth.currentUser
                        startDestination = if (currentUser != null) {
                            // 如果用戶已登錄，跳轉到主頁面
                            NavigationDestination.Main.route
                        } else {
                            // 如果用戶未登錄，跳轉到註冊頁面
                            NavigationDestination.Registration.route
                        }
                    }

                    // 只有在 startDestination 確定後才加載 NavHost
                    if (startDestination != null) {
                        AppNavigation(startDestination!!) // 傳遞初始路由
                    } else {
                        // 可以顯示一個加載指示器，或者保持空白直到 startDestination 被設置
                        // 例如: CircularProgressIndicator()
                    }
                }
            }
        }
    }
}