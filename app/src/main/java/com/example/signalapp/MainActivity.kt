package com.example.signalapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.signalapp.ui.theme.SignalAppTheme
import androidx.lifecycle.viewmodel.compose.viewModel // 確保導入
import androidx.navigation.NavHostController
import com.example.signalapp.navigation.NavRoutes
import com.example.signalapp.ui.MainAppScreen
import com.example.signalapp.ui.auth.LoginScreen
import com.example.signalapp.viewmodel.AuthViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.collect
import your_package_name.ui.auth.RegistrationScreen

class MainActivity : ComponentActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private var authStateListener: FirebaseAuth.AuthStateListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        setContent {
            SignalAppTheme {
                AppNavigation()
            }
        }
    }

    // --- AuthStateListener 管理 ---
    // 在 onStart 中添加監聽器
    override fun onStart() {
        super.onStart()
        // NavController 應該在 Listener 觸發時可用
        // 這部分有點 tricky，我們在 Composable 中處理狀態變化更 Compose 化
        // 我們將在 AppNavigation 中使用 LaunchedEffect 監聽狀態
        /*
        authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            // 在這裡處理導航比較麻煩，因為 NavController 在 Composable 裡
            // 我們改用在 Composable 裡監聽 Flow 的方式
            println("AuthState changed: ${user?.uid}")
        }
        firebaseAuth.addAuthStateListener(authStateListener!!)
        */
    }

    // 在 onStop 中移除監聽器
    override fun onStop() {
        super.onStop()
        // authStateListener?.let { firebaseAuth.removeAuthStateListener(it) }
    }
}

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(), // 創建 NavController
    authViewModel: AuthViewModel = viewModel() // 獲取共享的 ViewModel
) {
    // --- 使用 LaunchedEffect 監聽 Firebase Auth 狀態 ---
    // 這比在 Activity 中使用 Listener 更符合 Compose 的方式
    LaunchedEffect(key1 = Unit) { // 只執行一次用於設置監聽
        FirebaseAuth.getInstance().authStateChanges.collect{ user ->
//            Firebase.auth.authStateChanges.collect{ user ->
            if (user == null) {
                // 用戶未登入或已登出
                // 確保不在登入/註冊畫面上時才導航，避免循環
                if (navController.currentDestination?.route != NavRoutes.LOGIN &&
                    navController.currentDestination?.route != NavRoutes.REGISTER
                ) {
                    println("User logged out, navigating to Login")
                    // 清除返回堆疊並導航到登入
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true // 避免重複創建 Login 畫面
                    }
                }
            } else {
                // 用戶已登入
                // 只有當前不在主畫面時才導航，避免重複導航
                if (navController.currentDestination?.route != NavRoutes.MAIN_APP) {
                    println("User logged in (${user.uid}), navigating to Main App")
                    navController.navigate(NavRoutes.MAIN_APP) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true // 避免重複創建 Main App 畫面
                    }
                }
            }
        }
    }

    // --- 決定起始路由 ---
    // 初始狀態需要讀取一次，因為 Flow 初始可能沒有值
    // 注意：直接讀取 currentUser 可能會在狀態監聽器之前執行，導致短暫看到登入頁再跳轉
    // 更好的方式可能是顯示一個加載畫面，等 AuthState 第一次回調後再決定路由
    // 但我們先用簡單的方式：
    val startDestination = remember {
        if (FirebaseAuth.getInstance().currentUser != null) {
            println("Initial Check: User logged in, starting at Main App")
            NavRoutes.MAIN_APP
        } else {
            println("Initial Check: User logged out, starting at Login")
            NavRoutes.LOGIN
        }
    }

    // --- 設置 NavHost ---
    NavHost(
        navController = navController,
        startDestination = startDestination, // 基於初始狀態設置起始畫面
        modifier = modifier
    ) {
        // 定義登入畫面
        composable(NavRoutes.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate(NavRoutes.REGISTER)
                },
                onNavigateToMainApp = {
                    // 通常由 AuthStateListener 處理，但為防萬一保留
                    println("LoginScreen triggered navigation to Main App")
                    navController.navigate(NavRoutes.MAIN_APP) {
                        // 從返回堆疊中移除 Login 和 Register
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // 定義註冊畫面
        composable(NavRoutes.REGISTER) {
            RegistrationScreen(
                authViewModel = authViewModel,
                onNavigateToLogin = {
                    navController.popBackStack() // 返回登入畫面
                },
                onNavigateToMainApp = {
                    // 通常由 AuthStateListener 處理，但為防萬一保留
                    println("RegistrationScreen triggered navigation to Main App")
                    navController.navigate(NavRoutes.MAIN_APP) {
                        // 從返回堆疊中移除 Login 和 Register
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // 定義主應用畫面
        composable(NavRoutes.MAIN_APP) {
            // 確保 authViewModel 傳遞下去以便登出
            MainAppScreen(
                authViewModel = authViewModel,
                onLoggedOut = {
                    // 這個回調現在基本不用了，因為 Listener 會處理
                    println("MainAppScreen triggered LoggedOut callback")
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(NavRoutes.MAIN_APP) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}