package com.example.signalapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.signalapp.ui.theme.SignalAppTheme
import androidx.lifecycle.viewmodel.compose.viewModel // 確保導入

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SignalAppTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = AppDestinations.CONVERSATION_LIST_ROUTE
                ) {
                    composable(route = AppDestinations.CONVERSATION_LIST_ROUTE) {
                        // ConversationListScreen 可能也需要 ViewModel，稍後處理
                        ConversationListScreen(navController = navController)
                    }

                    composable(
                        route = AppDestinations.chatRouteWithArgs, // 路由模板不變
                        arguments = listOf(navArgument(AppDestinations.CHAT_ID_KEY) {
                            type = NavType.StringType
                        })
                    ) { // backStackEntry 參數仍然可用，但 ChatScreen 不直接需要它了
                        // --- 直接調用 ChatScreen ---
                        // viewModel() 委托會自動創建 ViewModel 實例，
                        // 並注入 SavedStateHandle，ViewModel 內部會獲取 conversationId
                        ChatScreen(navController = navController) // 不再需要傳遞 conversationId
                    }
                }
            }
        }
    }
}