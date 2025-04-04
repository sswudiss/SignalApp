package com.example.signalapp.navigation

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.signalapp.MainScreen
import com.example.signalapp.auth.*
import com.example.signalapp.auth.RegistrationScreen
import com.example.signalapp.screens.chat.ChatDetailScreen
import com.example.signalapp.shared.ConnectionViewModel
import com.google.firebase.auth.FirebaseAuth


// 將導航邏輯封裝在一個 Composable 中
@Composable
fun AppNavigation(startDestination: String) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = startDestination // 使用動態確定的起始頁面
    ) {

        // --- 註冊頁面 ---
        composable(NavigationDestination.Registration.route) {
            RegistrationScreen(
                onRegisterSuccess = {
                    navController.navigate(NavigationDestination.Main.route) {
                        popUpTo(NavigationDestination.Registration.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(NavigationDestination.Login.route) {
                        // 從註冊跳轉到登錄，不需要 popUpTo，除非你想讓返回鍵回到登錄前的狀態
                        launchSingleTop = true // 避免重複創建
                    }
                }
            )
        }

        // --- 登錄頁面 ---
        composable(NavigationDestination.Login.route) { // *** 使用 NavigationDestination ***
            val connectionViewModel: ConnectionViewModel = viewModel(
                viewModelStoreOwner = LocalActivity.current as ComponentActivity
            )
            LoginScreen(
                loginViewModel = viewModel(),
                connectionViewModel = connectionViewModel, // 傳遞給 LoginScreen
                onLoginSuccess = {
                    navController.navigate(NavigationDestination.Main.route) { // *** 使用 NavigationDestination ***
                        // 清空 Auth 相關頁面的棧
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        // 或者更明確地清空登錄和註冊（如果它們在棧中）
                        // popUpTo(NavigationDestination.Login.route) { inclusive = true }
                        // popUpTo(NavigationDestination.Registration.route) { inclusive = true } // 以防萬一
                        launchSingleTop = true
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(NavigationDestination.Registration.route) { // *** 使用 NavigationDestination ***
                        popUpTo(NavigationDestination.Login.route) {
                            inclusive = true
                        } // *** 使用 NavigationDestination ***
                        launchSingleTop = true
                    }
                }
            )
        }

        // --- 主頁面 ---
        composable(NavigationDestination.Main.route) { // *** 使用 NavigationDestination ***
            // MainScreen 可能不需要直接訪問 ConnectionViewModel，取決於其內部頁面
            // val connectionViewModel: ConnectionViewModel = viewModel(viewModelStoreOwner = LocalContext.current as ComponentActivity)
            MainScreen(
                // 如果 MainScreen 內部頁面需要，可以考慮傳遞，或者內部頁面自己獲取
                // connectionViewModel = connectionViewModel,
                onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    navController.navigate(NavigationDestination.Login.route) { // *** 使用 NavigationDestination ***
                        popUpTo(NavigationDestination.Main.route) {
                            inclusive = true
                        } // *** 使用 NavigationDestination ***
                        launchSingleTop = true
                    }
                },
                navigateToChatDetail = { contactId ->
                    // *** 使用 NavigationDestination 的輔助函數 ***
                    navController.navigate(NavigationDestination.ChatDetail.createRoute(contactId))
                    println("Navigating to chat detail for $contactId")
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
                // 假設你已經創建了 ChatDetailScreen
                ChatDetailScreen(contactId = contactId, navController = navController) // 使用你自己的實現
                // 如果還沒有 ChatDetailScreen，可以用一個佔位符
                // Box(modifier=Modifier.fillMaxSize()){ Text("Chat Detail for $contactId") }
            } else {
                // 處理 contactId 為 null 的情況，例如返回或顯示錯誤
                Text("錯誤：缺少聯繫人 ID")
                LaunchedEffect(Unit) { navController.popBackStack() } // 嘗試返回上一頁
            }
        }
    }
}