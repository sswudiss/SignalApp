package com.example.signalapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.signalapp.navigation.AppNavigation
import com.example.signalapp.navigation.NavigationDestination
import com.example.signalapp.shared.ConnectionViewModel
import com.example.signalapp.ui.theme.JJLLTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import androidx.activity.viewModels // 導入 activity-ktx 的 viewModels 委託
import androidx.lifecycle.viewmodel.compose.viewModel


class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    // 使用 Activity KTX 的委託來獲取 Activity 範圍的 ViewModel 實例
    private val connectionViewModel: ConnectionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth // 初始化 Firebase Auth

        setContent {
            JJLLTheme { // 應用我們定義的主題
                // 獲取 ConnectionViewModel 的實例 (Compose 方式，確保在 Activity 範圍內是同一個)
                 val connectionViewModel: ConnectionViewModel = viewModel() // 這個也可以用
                // *** 正確觀察 ConnectionViewModel 的狀態 ***
                // 使用 'by' 委託直接觀察 errorMessage State<String?>
                val connectionErrorMessage by connectionViewModel::errorMessage

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 檢查登錄狀態並設置初始路由
                    var startDestination by remember { mutableStateOf<String?>(null) }

                    LaunchedEffect(key1 = Unit) {
                        // 可以在這裡加一點延遲，確保Firebase Auth初始化完成，但非必需
                        delay(100) // 短暫延遲確保 Firebase 初始化
                        val currentUser = auth.currentUser
                        startDestination = if (currentUser != null) {
                            NavigationDestination.Main.route
                        } else {
                            NavigationDestination.Registration.route
                        }
                        println("Check user state: ${currentUser?.uid}, start destination: $startDestination")
                    }

                    // *** 使用 Column 包含錯誤橫幅和主要內容 ***
                    Column(modifier = Modifier.fillMaxSize()) {

                        // --- 全局錯誤提示區域 ---
                        // 使用 AnimatedVisibility 實現平滑顯示/隱藏
                        AnimatedVisibility(
                            visible = connectionErrorMessage != null,
                            enter = expandVertically(animationSpec = tween(300)),
                            exit = shrinkVertically(animationSpec = tween(300))
                        ) {
                            // 如果 connectionErrorMessage 可能為 null，提供一個默認值
                            GlobalErrorBanner(
                                message = connectionErrorMessage ?: "", // 確保非 null
                                onDismiss = { connectionViewModel.clearConnectionError() } // 允許用戶手動關閉
                            )
                        }

                        // --- 主要內容區域 (NavHost) ---
                        Box(modifier = Modifier.weight(1f)) { // 讓 NavHost 填充剩餘空間
                            if (startDestination != null) {
                                // *** 將 ConnectionViewModel 實例傳遞給 AppNavigation ***
                                // 注意：AppNavigation 需要修改以接收此參數，或者在內部獲取
                                // 我們先假設 AppNavigation 不需要這個參數，內部自行獲取
                                // 將 connectionViewModel 傳遞下去，以便子頁面可以調用 showError
                                AppNavigation(
                                    startDestination = startDestination!!,
                                    // 如果 AppNavigation 需要，則取消註釋: connectionViewModel = connectionViewModel
                                )
                            } else {
                                // 加載指示器
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- 新增：全局錯誤提示條 Composable ---
@Composable
fun GlobalErrorBanner(message: String, onDismiss: (() -> Unit)? = null) { // 允許有關閉選項
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)) // 紅色背景，帶一點透明
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween // 將文本和可能的關閉按鈕分開
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onErrorContainer, // 錯誤文字顏色
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f, fill = false) // 文本內容優先
                .padding(end = 8.dp) // 與關閉按鈕間隔
        )
        if (onDismiss != null) {
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "關閉錯誤提示",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}
