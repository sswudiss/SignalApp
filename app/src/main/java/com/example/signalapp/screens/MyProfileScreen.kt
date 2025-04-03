package com.example.signalapp.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.TextFields // 導入字體圖標
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter // 需要添加 Coil 依賴來加載圖片
import coil.request.ImageRequest
import com.example.signalapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

// 依賴添加 (在 app/build.gradle.kts):
// implementation("io.coil-kt:coil-compose:2.5.0") // 使用最新版本 Coil

@Composable
fun MyProfileScreen(
    // 依賴注入回調函數
    onLogout: () -> Unit,
    onChangePassword: () -> Unit,
    onEditProfile: () -> Unit, // 編輯頭像/名稱
    onSetFontSize: () -> Unit,
    onSetLanguage: () -> Unit,
    onAbout: () -> Unit
) {
    val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    // 從郵箱地址提取用戶名（去掉 @cruise.com）
    val username = currentUser?.email?.substringBefore('@') ?: "未知用戶"
    // 獲取用戶頭像 URL (如果有的話，Firebase Auth 可以存儲 photoUrl)
    val photoUrl = currentUser?.photoUrl

    // 為了頁面滾動
    val scrollState = rememberScrollState()

    // 確認登出的對話框狀態
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState) // 使內容可滾動
            .padding(bottom = 16.dp) // 底部留些空間
    ) {
        // 1. 頭像和名稱區域
        ProfileHeader(
            username = username,
            photoUrl = photoUrl?.toString(), // 將 Uri 轉為 String
            onEditProfileClick = onEditProfile // 點擊整個區域觸發編輯
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp)) // 分隔線

        // 2. 功能列表
        ProfileMenuItem(
            icon = Icons.Outlined.Lock,
            text = "修改密碼",
            onClick = onChangePassword // 點擊觸發修改密碼回調
        )
        ProfileMenuItem(
            icon = Icons.Outlined.TextFields, // 字體大小圖標
            text = "設置字體大小",
            onClick = onSetFontSize // 點擊觸發設置字體大小回調
        )
        ProfileMenuItem(
            icon = Icons.Outlined.Language, // 語言圖標
            text = "多國語言選擇",
            onClick = onSetLanguage // 點擊觸發語言選擇回調
        )
        ProfileMenuItem(
            icon = Icons.Outlined.Info, // 關於圖標
            text = "關於 JJLL",
            onClick = onAbout // 點擊觸發關於頁面回調
        )

        Spacer(modifier = Modifier.weight(1f)) // 將登出按鈕推到底部 (如果需要的話)

        // 3. 登出按鈕和提示
        LogoutSection(
            onLogoutClick = { showLogoutDialog = true } // 點擊顯示確認對話框
        )
    }

    // 登出確認對話框
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false }, // 點擊外部或返回鍵關閉
            title = { Text("確認登出") },
            text = { Text("您確定要登出嗎？如果忘記用戶名和密碼，您可能無法重新登錄。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        onLogout() // 確認登出，執行回調
                    }
                ) {
                    Text("確認", color = MaterialTheme.colorScheme.error) // 強調確認按鈕
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showLogoutDialog = false } // 取消按鈕
                ) {
                    Text("取消")
                }
            }
        )
    }
}

// 頭像和名稱組件
@Composable
fun ProfileHeader(
    username: String,
    photoUrl: String?,
    onEditProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEditProfileClick) // 整行可點擊編輯
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 頭像
        // 使用 Coil 加載網絡圖片或顯示佔位符
        val painter = rememberAsyncImagePainter(
            ImageRequest.Builder(LocalContext.current)
                .data(photoUrl) // 圖片 URL
                .error(R.drawable.ic_default_avatar) // 加載失敗時顯示的 Drawable (需要創建這個資源)
                .placeholder(R.drawable.ic_default_avatar) // 加載中顯示的 Drawable
                .crossfade(true) // 淡入效果
                .build(),
            // 如果沒有 URL，嘗試顯示用戶名首字母作為後備
            // onLoading = { }, // 加載中狀態回調
            onError = { /* 加載錯誤 */ } // 加載錯誤回調
        )

        // 如果沒有圖片 URL 並且 painter 加載失敗，顯示用戶名首字母
        val showInitials = photoUrl == null // 簡單判斷，更可靠的判斷應結合 Coil 的狀態

        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape) // 圓形裁剪
                .background(MaterialTheme.colorScheme.surfaceVariant), // 背景色
            contentAlignment = Alignment.Center
        ) {
            if (showInitials) {
                Text(
                    text = username.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Image(
                    painter = painter,
                    contentDescription = "用戶頭像",
                    contentScale = ContentScale.Crop, // 裁剪圖片以填充
                    modifier = Modifier.fillMaxSize()
                )
            }
        }


        Spacer(modifier = Modifier.width(16.dp))

        // 用戶名
        Text(
            text = username,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.weight(1f)) // 將編輯圖標推到右側

        // 編輯圖標提示用戶可以點擊
        Icon(
            imageVector = Icons.Outlined.Edit,
            contentDescription = "編輯個人資料",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}


// 菜單列表項組件
@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick) // 使列表項可點擊
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text, // 用文字作為無障礙描述
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary // 圖標顏色
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f) // 文字佔據主要空間
        )
        Icon(
            imageVector = Icons.Default.ChevronRight, // 右箭頭圖標
            contentDescription = "進入",
            tint = MaterialTheme.colorScheme.onSurfaceVariant // 箭頭顏色稍暗
        )
    }
}

// 登出部分組件
@Composable
fun LogoutSection(
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally // 內容居中
    ) {
        Button(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error // 紅色背景
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.ExitToApp,
                contentDescription = null, // 按鈕文字已說明意圖
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("登出賬號", color = MaterialTheme.colorScheme.onError) // 白色文字
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "提醒：如果忘記用戶名和密碼，您可能無法重新登錄。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant // 使用次要文字顏色
        )
    }
}
