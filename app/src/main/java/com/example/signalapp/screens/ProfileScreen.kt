package com.example.signalapp.screens


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.* // 導入需要的圖標
import androidx.compose.material.icons.outlined.* // 導入 Outlined 圖標
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth
import com.example.signalapp.R

@Composable
fun ProfileScreen(
    onLogout: () -> Unit // 從外部傳入登出處理邏輯
) {
    // 獲取當前 Firebase 用戶
    val currentUser = FirebaseAuth.getInstance().currentUser
    var username by remember { mutableStateOf("用戶名加載中...") }
    // 可以添加用戶頭像 URL 狀態
    // var userAvatarUrl by remember { mutableStateOf<String?>(null) }

    // 從 currentUser 提取信息 (只執行一次或當 currentUser 變化時)
    LaunchedEffect(currentUser) {
        username = currentUser?.email?.substringBefore('@') ?: "未知用戶"
        // 實際應用中，頭像 URL 可能存儲在 Firestore 或 Firebase Realtime Database
        // userAvatarUrl = currentUser?.photoUrl?.toString()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // 使頁面可滾動
            .padding(vertical = 16.dp) // 上下邊距
    ) {
        // --- 用戶信息區域 ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    // .data(userAvatarUrl) // 使用用戶頭像 URL
                    .data(null) // 暫時為空
                    .crossfade(true)
                    .placeholder(R.drawable.ic_launcher_foreground) // 需要佔位符
                    .error(R.drawable.ic_launcher_foreground) // 需要佔位符
                    .build(),
                contentDescription = "用戶頭像",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
            )
            // Icon(
            //     imageVector = Icons.Filled.AccountCircle,
            //     contentDescription = "用戶頭像",
            //     modifier = Modifier.size(64.dp),
            //     tint = MaterialTheme.colorScheme.primary
            // )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = username,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
        }

        Divider(thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))

        // --- 設置選項列表 ---
        SettingItem(
            icon = Icons.Outlined.Person,
            title = "賬戶",
            onClick = { /* TODO: 導航到賬戶設置 */ }
        )
        SettingItem(
            icon = Icons.Outlined.Palette, // Material Design Icons 庫裡的圖標
            title = "外觀",
            onClick = { /* TODO: 導航到外觀設置 */ }
        )
        SettingItem(
            icon = Icons.Outlined.Notifications,
            title = "通知",
            onClick = { /* TODO: 導航到通知設置 */ }
        )
        SettingItem(
            icon = Icons.Outlined.Storage,
            title = "數據與存儲",
            onClick = { /* TODO: 導航到數據設置 */ }
        )
        SettingItem(
            icon = Icons.Outlined.HelpOutline,
            title = "幫助",
            onClick = { /* TODO: 顯示幫助信息 */ }
        )
        SettingItem(
            icon = Icons.Outlined.Info,
            title = "關於",
            onClick = { /* TODO: 顯示關於頁面 */ }
        )

        Spacer(modifier = Modifier.weight(1f)) // 將登出按鈕推到底部 (如果空間允許)

        // --- 登出按鈕 ---
        Button(
            onClick = onLogout, // <--- 調用傳入的登出函數
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) // 使用錯誤顏色提示危險操作
        ) {
            Icon(Icons.Filled.Logout, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("登出")
        }
    }
}

// 設置選項的通用 Composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(title, fontSize = 16.sp) },
        leadingContent = { Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary) }
    )
    Divider(modifier = Modifier.padding(start = 16.dp + 24.dp + 16.dp)) // 從文本開始的位置添加分隔線
}