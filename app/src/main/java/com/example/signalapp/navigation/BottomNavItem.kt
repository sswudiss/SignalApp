package com.example.signalapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat // 使用 Material Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

// 密封類定義底部導航的項目
sealed class BottomNavItem(
    val route: String, // 內部導航路由
    val title: String, // 標題文字
    val icon: ImageVector // 圖標
) {
    object Chat : BottomNavItem("chat_list", "聊天", Icons.Filled.Chat)
    object Contacts : BottomNavItem("contacts", "通訊錄", Icons.Filled.Contacts)
    object MyProfile : BottomNavItem("my_profile", "我的", Icons.Filled.Person)
}