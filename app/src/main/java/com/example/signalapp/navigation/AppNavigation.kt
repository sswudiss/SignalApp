package com.example.signalapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.signalapp.MainScreen
import com.example.signalapp.auth.LoginScreen
import com.example.signalapp.auth.RegistrationScreen

// 將導航邏輯封裝在一個 Composable 中
@Composable
fun AppNavigation(startDestination: String) {
    val navController = rememberNavController() // 創建 NavController

    NavHost(
        navController = navController,
        startDestination = startDestination // 使用動態確定的起始頁面
    ) {
        // 定義註冊頁面路由
        composable(NavigationDestination.Registration.route) {
            RegistrationScreen(
                onRegisterSuccess = {
                    // 註冊成功後跳轉到主頁面，並清除註冊和登錄頁面的回退棧
                    navController.navigate(NavigationDestination.Main.route) {
                        popUpTo(NavigationDestination.Registration.route) { inclusive = true } // 從回退棧中移除註冊頁
                        launchSingleTop = true // 避免在棧頂重複創建 MainScreen
                    }
                },
                onNavigateToLogin = {
                    // 從註冊頁跳轉到登錄頁
                    navController.navigate(NavigationDestination.Login.route)
                }
            )
        }
        // 定義登錄頁面路由
        composable(NavigationDestination.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    // 登錄成功後跳轉到主頁面，並清除登錄和註冊頁面的回退棧
                    navController.navigate(NavigationDestination.Main.route) {
                        popUpTo(NavigationDestination.Login.route) { inclusive = true } // 從回退棧中移除登錄頁
                        // 如果是從註冊跳轉過來的，也需要移除註冊頁，可以考慮 popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true // 避免在棧頂重複創建 MainScreen
                    }
                },
                onNavigateToRegister = {
                    // 從登錄頁跳轉回註冊頁
                    navController.navigate(NavigationDestination.Registration.route) {
                        popUpTo(NavigationDestination.Login.route) { inclusive = true } // 從回退棧中移除登錄頁
                    }
                }
            )
        }
        // 定義主頁面路由 (稍後實現 MainScreen)
        composable(NavigationDestination.Main.route) {
            MainScreen(
                onLogout = {
                    // 登出後返回登錄頁面，並清除所有回退棧
                    navController.navigate(NavigationDestination.Login.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                navigateToChatDetail = { contactId ->
                    // 導航到聊天詳情頁 (稍後實現)
                    navController.navigate(NavigationDestination.ChatDetail.createRoute(contactId))
                }
            )
        }
        // 定義聊天詳情頁路由 (稍後實現)
        composable(
            route = NavigationDestination.ChatDetail.route,
            arguments = NavigationDestination.ChatDetail.arguments // 定義參數
        ) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getString("contactId") ?: return@composable // 安全獲取參數
            // ChatDetailScreen(contactId = contactId, navController = navController) // 稍後創建 ChatDetailScreen
            // 臨時佔位符
            // Text("Chat Detail Screen for $contactId")
        }
    }
}