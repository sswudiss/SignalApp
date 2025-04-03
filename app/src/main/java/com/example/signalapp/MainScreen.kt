package com.example.signalapp

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.signalapp.screens.ChatListScreen
import com.example.signalapp.navigation.BottomNavItem
import com.example.signalapp.screens.ContactsScreen
import com.example.signalapp.screens.MyProfileScreen
import com.example.signalapp.ui.theme.JJLLTheme

@OptIn(ExperimentalMaterial3Api::class) // CenterAlignedTopAppBar 是實驗性 API
@Composable
fun MainScreen(
    // 從 MainActivity 傳入的回調，用於處理外部導航（登出、跳轉詳情）
    onLogout: () -> Unit,
    navigateToChatDetail: (String) -> Unit // 接收聯繫人ID
) {
    // 內部導航控制器，用於底部導航欄切換頁面
    val internalNavController = rememberNavController()

    // 獲取當前路由，用於判斷是否顯示 TopAppBar 和選中底部標籤
    val navBackStackEntry by internalNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // 判斷當前是否在“我的”頁面
    val isMyProfileScreen = currentDestination?.hierarchy?.any { it.route == BottomNavItem.MyProfile.route } == true

    Scaffold(
        // 根據是否為“我的”頁面決定是否顯示 TopAppBar
        topBar = {
            if (!isMyProfileScreen) {
                MainTopAppBar(
                    onAvatarClick = {
                        // 點擊頭像，發送信息給自己（實現方式待定，可以是打開與自己的聊天）
                        println("Avatar clicked! Navigate to self chat.")
                        // 示例：可以導航到一個特殊的聊天 ID，如 "self"
                        navigateToChatDetail("self_user_id") // 需要確定如何標識自己
                    },
                    onSearchClick = {
                        // TODO: 處理搜索邏輯
                        println("Search clicked!")
                    },
                    onMenuClick = {
                        // TODO: 處理菜單邏輯 (例如顯示下拉菜單)
                        println("Menu clicked!")
                    }
                )
            }
        },
        bottomBar = {
            MainBottomNavigation(
                navController = internalNavController,
                currentDestination = currentDestination
            )
        }
    ) { innerPadding -> // Scaffold 內容區域的內邊距
        // 內部導航主機，根據底部導航切換內容
        InternalNavHost(
            navController = internalNavController,
            paddingValues = innerPadding, // 將 Scaffold 的 padding 傳遞給 NavHost 內容
            onLogout = onLogout, // 將登出回調向下傳遞
            navigateToChatDetail = navigateToChatDetail // 將跳轉詳情回調向下傳遞
        )
    }
}

// 頂部應用欄 Composable
@OptIn(ExperimentalMaterial3Api::class) // CenterAlignedTopAppBar 是實驗性 API
@Composable
fun MainTopAppBar(
    onAvatarClick: () -> Unit,
    onSearchClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar( // Material 3 的居中標題 TopAppBar
        title = { /* 這裡可以留空，或者放 App 名稱 */ },
        modifier = modifier,
        navigationIcon = {
            // 用戶頭像（暫用圖標代替，後續替換）
            IconButton(onClick = onAvatarClick) {
                Icon(
                    imageVector = Icons.Filled.AccountCircle, // 臨時頭像圖標
                    contentDescription = "用戶頭像 (發送給自己)"
                )
            }
        },
        actions = {
            // 搜索圖標
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "搜索"
                )
            }
            // 菜單選項圖標
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Filled.MoreVert, // 或者 Icons.Filled.Menu
                    contentDescription = "菜單選項"
                )
            }
        },
        // 可以設置顏色等
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer, // 設置背景色
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer, // 標題顏色
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer, // 導航圖標顏色
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer // 操作圖標顏色
        )
    )
}

// 底部導航欄 Composable
@Composable
fun MainBottomNavigation(
    navController: NavHostController,
    currentDestination: androidx.navigation.NavDestination? // 注意導入 NavDestination
) {
    val items = listOf(
        BottomNavItem.Chat,
        BottomNavItem.Contacts,
        BottomNavItem.MyProfile
    )

    NavigationBar { // Material 3 的底部導航組件
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true, // 判斷是否選中
                onClick = {
                    navController.navigate(screen.route) {
                        // 點擊已選中的項目或重新選擇起始項目時，恢復到該項目的起始狀態
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true // 保存回退棧狀態
                        }
                        // 避免在回退棧頂部重複創建同一個目標
                        launchSingleTop = true
                        // 重新選擇已選項時恢復其狀態
                        restoreState = true
                    }
                }
            )
        }
    }
}

// 內部導航主機 Composable
@Composable
fun InternalNavHost(
    navController: NavHostController,
    paddingValues: PaddingValues,
    onLogout: () -> Unit, // 接收登出回調
    navigateToChatDetail: (String) -> Unit // 接收跳轉詳情回調
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Chat.route, // 默認顯示聊天列表
        modifier = Modifier.padding(paddingValues) // 應用 Scaffold 提供的 Padding
    ) {
        composable(BottomNavItem.Chat.route) {
            // 聊天列表頁面 (需要創建這個 Composable)
            ChatListScreen(
                onNavigateToChat = navigateToChatDetail // 將跳轉邏輯傳遞下去
            )
        }
        composable(BottomNavItem.Contacts.route) {
            // 通訊錄頁面 (需要創建這個 Composable)
            ContactsScreen(
                onNavigateToChat = navigateToChatDetail // 通訊錄也可以發起聊天
            )
        }
        composable(BottomNavItem.MyProfile.route) {
            // “我的”頁面 (需要創建這個 Composable)
            MyProfileScreen(
                onLogout = onLogout, // 將登出回調傳遞給“我的”頁面
                // 其他需要從 MyProfileScreen 觸發的操作回調...
                onChangePassword = { /* TODO: 實現修改密碼邏輯 */ },
                onEditProfile = { /* TODO: 實現編輯個人資料邏輯 */ },
                onSetFontSize = { /* TODO: 實現設置字體大小邏輯 */ },
                onSetLanguage = { /* TODO: 實現設置語言邏輯 */ },
                onAbout = { /* TODO: 顯示關於信息 */ }
            )
        }
    }
}

// --- 佔位符 Composable (稍後創建實際的屏幕) ---
@Composable
fun PlaceholderScreen(name: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = name)
    }
}

// ChatListScreen (佔位符)
// @Composable
// fun ChatListScreen(onNavigateToChat: (String) -> Unit) {
//     PlaceholderScreen("聊天列表 (點擊某個聊天會調用 onNavigateToChat)")
//     // 實際實現：顯示聊天記錄列表，點擊項目時調用 onNavigateToChat(contactId)
//     // Button(onClick = { onNavigateToChat("contact_123") }) { Text("打開聊天 contact_123")}
// }

// ContactsScreen (佔位符)
// @Composable
// fun ContactsScreen(onNavigateToChat: (String) -> Unit) {
//      PlaceholderScreen("通訊錄 (點擊聯繫人會調用 onNavigateToChat)")
//      // 實際實現：顯示聯繫人列表，點擊項目時調用 onNavigateToChat(contactId)
//      // Button(onClick = { onNavigateToChat("contact_456") }) { Text("與 contact_456 聊天")}
// }

// MyProfileScreen (佔位符，增加登出按鈕)
// @Composable
// fun MyProfileScreen(
//     onLogout: () -> Unit,
//     onChangePassword: () -> Unit,
//     onEditProfile: () -> Unit,
//     onSetFontSize: () -> Unit,
//     onSetLanguage: () -> Unit,
//     onAbout: () -> Unit
// ) {
//     Column(
//         modifier = Modifier.fillMaxSize(),
//         horizontalAlignment = Alignment.CenterHorizontally,
//         verticalArrangement = Arrangement.Center
//     ) {
//         PlaceholderScreen("我的頁面")
//         Spacer(modifier = Modifier.height(20.dp))
//         Button(onClick = onLogout, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
//             Text("登出賬號", color = MaterialTheme.colorScheme.onError)
//         }
//         // 其他按鈕佔位
//         Button(onClick = onEditProfile) { Text("編輯頭像/名稱") }
//         Button(onClick = onChangePassword) { Text("修改密碼") }
//         Button(onClick = onSetFontSize) { Text("設置字體大小") }
//         Button(onClick = onSetLanguage) { Text("多國語言選擇") }
//         Button(onClick = onAbout) { Text("關於APP") }
//     }
// }

// --- Preview ---
@RequiresApi(Build.VERSION_CODES.S)
@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    JJLLTheme {
        // 在預覽中，我們需要模擬外部導航回調
        MainScreen(
            onLogout = { println("Preview Logout") },
            navigateToChatDetail = { contactId -> println("Preview Navigate to Chat $contactId") }
        )
    }
}

// 單獨預覽 TopAppBar
@RequiresApi(Build.VERSION_CODES.S)
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF) // 設置背景色方便預覽
@Composable
fun MainTopAppBarPreview() {
    JJLLTheme {
        MainTopAppBar(onAvatarClick = {}, onSearchClick = {}, onMenuClick = {})
    }
}

// 單獨預覽 BottomNavigation
@RequiresApi(Build.VERSION_CODES.S)
@Preview(showBackground = true)
@Composable
fun MainBottomNavigationPreview() {
    JJLLTheme {
        // 預覽需要一個 NavController，這裡創建一個臨時的
        val previewNavController = rememberNavController()
        // 預覽時可以手動設置一個 currentDestination 來觀察選中效果
        // 但更簡單的方式是直接渲染組件
        MainBottomNavigation(navController = previewNavController, currentDestination = null)
    }
}