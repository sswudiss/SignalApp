package com.example.signalapp.navigation

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.signalapp.MainScreen
import com.example.signalapp.auth.LoginScreen
import com.example.signalapp.auth.RegistrationScreen
import com.example.signalapp.screens.chat.ChatDetailScreen
import com.example.signalapp.shared.ConnectionViewModel
import com.google.firebase.auth.FirebaseAuth


// 將導航邏輯封裝在一個 Composable 中
@Composable
fun AppNavigation(
    startDestination: String,
    connectionViewModel: ConnectionViewModel // 接收 ViewModel
) {
    val navController = rememberNavController() // 創建 NavController

    NavHost(
        navController = navController,
        startDestination = startDestination // 使用動態確定的起始頁面
    ) {
        // 定義註冊頁面路由
        composable(NavigationDestination.Registration.route) {
            RegistrationScreen(
                // 直接訪問 Activity 範圍的 ViewModel
                connectionViewModel = viewModel(viewModelStoreOwner = LocalActivity.current as ComponentActivity),

                onRegisterSuccess = {
                    // 註冊成功後跳轉到主頁面，並清除註冊和登錄頁面的回退棧
                    navController.navigate(NavigationDestination.Main.route) {
                        popUpTo(NavigationDestination.Registration.route) {
                            inclusive = true
                        } // 從回退棧中移除註冊頁
                        launchSingleTop = true // 避免在棧頂重複創建 MainScreen
                    }
                },
                onNavigateToLogin = {
                    // 從註冊頁跳轉到登錄頁
                    navController.navigate(NavigationDestination.Login.route) {
                        // 從註冊跳轉到登錄，不需要 popUpTo，除非你想讓返回鍵回到登錄前的狀態
                        launchSingleTop = true // 避免重複創建
                    }
                }
            )
        }

        // 定義登錄頁面路由
        composable(NavigationDestination.Login.route) {
            LoginScreen(
                // connectionViewModel = connectionViewModel, // 傳遞
                connectionViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity),
                onLoginSuccess = {
                    // 登錄成功後跳轉到主頁面，並清除登錄和註冊頁面的回退棧
                    navController.navigate(NavigationDestination.Main.route) {
                        // 清除登錄和可能的註冊頁面
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        } // 從回退棧中移除登錄頁
                        // 如果是從註冊跳過來的，startDestinationId 可能是 Registration，所以要確保清掉 Auth 相關的棧
                        // 考慮更健壯的清空Auth流程的棧：
                        // popUpTo(Screen.Login.route) { inclusive = true } // 清掉登錄頁
                        // popUpTo(Screen.Registration.route) { inclusive = true } // 如果存在，也清掉註冊頁 (如果 Login 不是由 Registration 啟動則此行無效)
                        // 最簡單的是 popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                        launchSingleTop = true // 避免在棧頂重複創建 MainScreen
                    }
                },
                onNavigateToRegister = {
                    // 從登錄頁跳轉回註冊頁
                    navController.navigate(NavigationDestination.Registration.route) {
                        popUpTo(NavigationDestination.Login.route) {
                            inclusive = true
                        } // 從回退棧中移除登錄頁
                    }
                }
            )
        }

        // 定義主頁面路由 (稍後實現 MainScreen)
        composable(NavigationDestination.Main.route) {
            MainScreen(
                //connectionViewModel = connectionViewModel, // 傳遞
                connectionViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity),
                onLogout = {
                    // *** 執行登出邏輯 ***
                    FirebaseAuth.getInstance().signOut() // 清除 Firebase 登錄狀態
                    // 導航回登錄頁，並清除 MainScreen 回退棧
                    navController.navigate(NavigationDestination.Login.route) {
                        popUpTo(NavigationDestination.Main.route) { inclusive = true } // 清除主頁面棧
                        // popUpTo(navController.graph.startDestinationId) { inclusive = true } // 也可以直接清空整個圖的棧回到起點（Login）
                        launchSingleTop = true
                    }
                },
                navigateToChatDetail = { contactId ->
                    // 導航到聊天詳情頁
                    navController.navigate(NavigationDestination.ChatDetail.createRoute(contactId))
                    println("Navigating to chat detail for $contactId") // 添加日誌
                }
            )
        }


        // --- 聊天詳情頁 --- (確保你已經導入 Screen.ChatDetail 和相關 Composable)
        composable(
            route = NavigationDestination.ChatDetail.route,
            arguments = NavigationDestination.ChatDetail.arguments // 定義參數
        ) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getString("contactId")
            println("Entering Chat Detail screen for $contactId") // 添加日誌
            if (contactId != null) {
                ChatDetailScreen(
                    contactId = contactId,
                    navController = navController,
                    // connectionViewModel = connectionViewModel, // 傳遞
                    chatDetailViewModel = viewModel(), // ChatDetail自己的ViewModel
                    // 在ChatDetailScreen內部訪問共享ViewModel:
                    connectionViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity)
                )} else {
                // 處理 contactId 為 null 的情況，例如返回或顯示錯誤
                Text("錯誤：缺少聯繫人 ID")
                LaunchedEffect(Unit) { navController.popBackStack() } // 嘗試返回上一頁
            }
        }
    }
}