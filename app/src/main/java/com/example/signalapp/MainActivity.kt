package com.example.signalapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.signalapp.ui.ChatListScreen
import com.example.signalapp.ui.ChatScreen
import com.example.signalapp.ui.auth.AuthScreen
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this) // 初始化 Firebase

        setContent {
            SignalApp()
        }
    }
}

@Composable
fun SignalApp() {
    val navController = rememberNavController()
    val user = Firebase.auth.currentUser // 獲取當前登入用戶

    NavHost(navController = navController, startDestination = if (user != null) "chatList" else "auth") {
        composable("auth") {
            AuthScreen { navController.navigate("chatList") { popUpTo("auth") { inclusive = true } } }
        }
        composable("chatList") {
            ChatListScreen(navController)
        }
        composable("chat/{chatId}") { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: return@composable
            ChatScreen(chatId)
        }
    }
}
