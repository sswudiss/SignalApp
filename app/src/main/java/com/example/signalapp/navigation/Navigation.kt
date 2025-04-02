package com.example.signalapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.ui.graphics.vector.ImageVector

// 導航路由常量
object Routes {
    // Screen routes
    const val CHAT_LIST = "chat_list"
    const val CONTACTS = "contacts"
    const val PROFILE = "profile"
    const val REGISTRATION = "registration"
    const val LOGIN = "login" // 預留登錄路由
    const val SPLASH = "splash" // 啟動/決策屏幕路由

    // Graph routes
    const val AUTH_GRAPH_ROUTE = "auth_graph"
    const val MAIN_GRAPH_ROUTE = "main_graph"
}
// 底部導航項目的數據模型
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Chat : BottomNavItem(
        route = Routes.CHAT_LIST,
        title = "聊天",
        selectedIcon = Icons.Filled.Chat,
        unselectedIcon = Icons.Outlined.Chat
    )
    object Contacts : BottomNavItem(
        route = Routes.CONTACTS,
        title = "通訊錄",
        selectedIcon = Icons.Filled.Contacts,
        unselectedIcon = Icons.Outlined.Contacts
    )
    object Profile : BottomNavItem( // "我的"
        route = Routes.PROFILE,
        title = "我的",
        selectedIcon = Icons.Filled.AccountCircle,
        unselectedIcon = Icons.Outlined.AccountCircle
    )
}