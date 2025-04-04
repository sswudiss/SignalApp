package com.example.signalapp.navigation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

// 使用你選擇的名稱 NavigationDestination
sealed class NavigationDestination(val route: String, val arguments: List<NamedNavArgument> = emptyList()) {
    object Registration : NavigationDestination("registration")
    object Login : NavigationDestination("login")
    object Main : NavigationDestination("main") // 主屏幕，包含底部導航

    // 聊天詳情頁，需要參數（例如聯繫人 ID）
    object ChatDetail : NavigationDestination(
        route = "chat_detail/{contactId}", // 路由模式，包含參數佔位符
        arguments = listOf(navArgument("contactId") { type = NavType.StringType }) // 定義參數類型
    ) {
        // 輔助函數，用於創建帶有實際參數的路由字符串
        fun createRoute(contactId: String) = "chat_detail/$contactId"
    }

    // 可以在這裡添加更多屏幕...
}