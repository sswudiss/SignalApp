package com.example.signalapp.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.signalapp.RegistrationScreen
import com.example.signalapp.RegistrationViewModel
import com.example.signalapp.screens.LoginScreen
import com.example.signalapp.screens.LoginViewModel
import com.example.signalapp.screens.MainScreen
import com.example.signalapp.screens.SplashScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun RootNavigationGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        route = "root_graph" // 給根圖一個名稱 (可選)
    ) {
        // 添加 Splash 屏幕的 composable
        composable(Routes.SPLASH) {
            SplashScreen(navController = navController)
        }

        // 認證流程導航圖 (無 Scaffold)
        navigation(
            startDestination = Routes.REGISTRATION, // 認證流程的起始頁是註冊
            route = Routes.AUTH_GRAPH_ROUTE
        ) {
            composable(Routes.REGISTRATION) {
                val registrationViewModel: RegistrationViewModel = viewModel()
                RegistrationScreen(
                    viewModel = registrationViewModel,
                    onNavigateToHome = {  // 註冊成功
                        // 導航到主流程
                        navController.navigate(Routes.MAIN_GRAPH_ROUTE) {
                            popUpTo(Routes.AUTH_GRAPH_ROUTE) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToLogin = { // <--- 傳遞導航到登錄的 lambda  // 去登錄頁
                        navController.navigate(Routes.LOGIN) {
                            // 從註冊到登錄，我們通常不 popUp，允許用戶返回
                            launchSingleTop = true // 避免重複創建 LoginScreen 實例
                            // 可選: popUpTo(Routes.REGISTRATION) { inclusive = true } // 如果希望從登錄返回時不回到註冊頁
                        }
                    }
                )
            }
            composable(Routes.LOGIN) {
                val loginViewModel: LoginViewModel = viewModel() // <--- 獲取 LoginViewModel
                LoginScreen(
                    viewModel = loginViewModel, // <--- 傳遞 ViewModel
                    onLoginSuccess = { // <--- 處理登錄成功
                        navController.navigate(Routes.MAIN_GRAPH_ROUTE) {
                            popUpTo(Routes.AUTH_GRAPH_ROUTE) { inclusive = true } // 清空認證流程
                            launchSingleTop = true
                        }
                    },
                    onNavigateToRegister = { // <--- 處理去註冊頁
                        navController.navigate(Routes.REGISTRATION) {
                            // 從登錄頁去註冊頁，通常會移除登錄頁
                            popUpTo(Routes.LOGIN) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        // 主應用流程導航圖
        composable(route = Routes.MAIN_GRAPH_ROUTE) {
            // 在這裡定義登出邏輯，因為需要根 NavController
            val mainOnLogout = {
                FirebaseAuth.getInstance().signOut() // 執行 Firebase 登出
                // 導航回認證流程，並清除主流程後退棧
                navController.navigate(Routes.AUTH_GRAPH_ROUTE) {
                    popUpTo(Routes.MAIN_GRAPH_ROUTE) { inclusive = true } // 清除主流程
                    launchSingleTop = true
                }
                println("用戶已登出")
            }
            // 將登出邏輯傳遞給 MainScreen
            MainScreen(onLogout = mainOnLogout)
        }
    }
}