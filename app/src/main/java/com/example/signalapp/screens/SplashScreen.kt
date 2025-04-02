package com.example.signalapp.screens


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.signalapp.navigation.Routes
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SplashScreen(navController: NavHostController) {
    // 這個 LaunchedEffect 只會在 Composable 首次進入組合時執行一次
    LaunchedEffect(key1 = true) {
        // 檢查當前的 Firebase 用戶
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            // 如果用戶已登錄，導航到主應用流程
            navController.navigate(Routes.MAIN_GRAPH_ROUTE) {
                // 從後退棧中移除 Splash 屏幕，避免按返回鍵回到這裡
                popUpTo(Routes.SPLASH) { inclusive = true }
                launchSingleTop = true // 可選，確保 Main graph 不重複創建
            }
        } else {
            // 如果用戶未登錄，導航到認證流程
            navController.navigate(Routes.AUTH_GRAPH_ROUTE) {
                // 從後退棧中移除 Splash 屏幕
                popUpTo(Routes.SPLASH) { inclusive = true }
                launchSingleTop = true // 可選
            }
        }
    }

    // 顯示一個簡單的加載指示器，表示正在檢查狀態
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
        // 或者可以留空，因為跳轉很快
    }
}