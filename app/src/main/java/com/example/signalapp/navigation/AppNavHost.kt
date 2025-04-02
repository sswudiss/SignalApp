package com.example.signalapp.navigation


import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.signalapp.RegistrationScreen // 導入註冊屏幕
import com.example.signalapp.RegistrationViewModel
import com.example.signalapp.screens.*

@Composable
fun AppNavHost(
    navController: NavHostController, // 這個 NavController 是從 MainScreen 傳來的
    paddingValues: PaddingValues, // 從 Scaffold 傳入 padding
    onLogout: () -> Unit // <--- 接收 onLogout 回調
) {
    NavHost(
        navController = navController,
        startDestination = Routes.CHAT_LIST, // 主應用的默認頁是聊天列表
        modifier = Modifier.padding(paddingValues) // 應用 padding 避免內容與 Bar 重疊
    ) {
        composable(Routes.CHAT_LIST) { ChatListScreen() }
        composable(Routes.CONTACTS) { ContactsScreen() }
        composable(Routes.PROFILE) {// 將 onLogout 回調傳遞給 ProfileScreen
            ProfileScreen(onLogout = onLogout) // <--- 傳遞給 ProfileScreen
        }
        // ... 其他主應用內部頁面
    }
}