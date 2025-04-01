package com.example.signalapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.signalapp.ui.chatlist.ChatListScreen
import com.example.signalapp.ui.registration.RegistrationScreen
import androidx.navigation.NavType              // 導入 NavType
import androidx.navigation.navArgument       // 導入 navArgument
import com.example.signalapp.ui.chatdetail.ChatDetailScreen
import com.example.signalapp.ui.newchat.NewChatScreen

@Composable
fun AppNavigationHost() {
    // 1. 創建 NavController 實例並記住它
    val navController = rememberNavController()

    // 2. 創建 NavHost
    NavHost(
        navController = navController, // 將 NavController 傳遞給 NavHost
        startDestination = AppDestinations.REGISTRATION_ROUTE // 設定起始畫面路由
    ) {
        // 註冊頁
        composable(AppDestinations.REGISTRATION_ROUTE) {
            RegistrationScreen(
                onRegistrationSuccess = {
                    navController.navigate(AppDestinations.CHAT_LIST_ROUTE) {
                        popUpTo(AppDestinations.REGISTRATION_ROUTE) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // 聊天列表頁
        composable(AppDestinations.CHAT_LIST_ROUTE) {
            ChatListScreen( // --- 修改：傳遞 navController ---
                navController = navController
                // 如果 ChatListScreen 需要 ViewModel，這裡不需要傳，它內部會用 viewModel() 獲取
            )
        }

        // --- 新增：聊天詳情頁 ---
        composable(
            route = AppDestinations.CHAT_DETAIL_PATH, // 使用包含參數的路徑
            arguments = listOf(navArgument(NavigationArgs.CHAT_ID) { // 定義參數
                type = NavType.StringType // 參數類型為 String
                // nullable = false // 默認不可空
                // defaultValue = "..." // 可以設置默認值
            })
        ) { backStackEntry -> // composable lambda 提供了 NavBackStackEntry
            // 從 backStackEntry 中提取參數
            val chatId = backStackEntry.arguments?.getString(NavigationArgs.CHAT_ID)
            // 安全處理，如果 chatId 為 null，可以導航回列表或顯示錯誤
            if (chatId != null) {
                ChatDetailScreen(
                    onNavigateBack = { navController.navigateUp() } // 使用 navigateUp() 進行返回
                )
            } else {
                // 處理錯誤情況，例如導航回列表頁
                // 這段代碼理論上不應執行，除非導航路徑構建錯誤且未傳遞 chatId
                // 可以選擇打印日誌、顯示錯誤頁或直接返回
                println("Error: chatId is null in chatDetail route!")
                navController.navigateUp() // 或者導航到一個安全的默認頁面
            }
        }

        // --- 新增：新聊天頁 ---
        composable(AppDestinations.NEW_CHAT_ROUTE) {
            NewChatScreen(
                navController = navController, // <--- 傳遞 NavController

            )
        }
    }
}