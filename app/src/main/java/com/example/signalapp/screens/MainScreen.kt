package com.example.signalapp.screens


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person // 暫替頭像
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.signalapp.navigation.AppNavHost // 我們稍後創建 AppNavHost
import com.example.signalapp.navigation.BottomNavItem
import com.example.signalapp.navigation.Routes
import com.example.signalapp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onLogout: () -> Unit) {
    // MainScreen 現在使用自己的 NavController 來管理底部導航
    val mainNavController = rememberNavController() // 注意：這裡用新的 NavController
    val bottomNavItems = listOf(
        BottomNavItem.Chat,
        BottomNavItem.Contacts,
        BottomNavItem.Profile
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(getScreenTitle(mainNavController)) }, // 動態標題
                navigationIcon = {
                    IconButton(onClick = { /* TODO: Handle profile click */ }) {
                        Icon(
                            imageVector = Icons.Default.Person, // 暫用 Person 圖標
                            contentDescription = stringResource(R.string.content_description_profile_picture)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO: Handle search click */ }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = stringResource(R.string.content_description_search)
                        )
                    }
                    IconButton(onClick = { /* TODO: Handle menu click */ }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = stringResource(R.string.content_description_menu)
                        )
                    }
                }
                // 可以根據需要添加 colors = TopAppBarDefaults.topAppBarColors(...)
            )
        },
        bottomBar = {
            NavigationBar { // Material 3 使用 NavigationBar
                val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                if (currentDestination?.hierarchy?.any { it.route == screen.route } == true) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.title
                            )
                        },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            mainNavController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(mainNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        /// 將 onLogout 傳遞給 AppNavHost
        AppNavHost(
            navController = mainNavController,
            paddingValues = innerPadding,
            onLogout = onLogout // <--- 往下傳遞
        )
    }
}

// 輔助函數：根據當前路由獲取屏幕標題 (基於傳入的 mainNavController)
@Composable
private fun getScreenTitle(navController: NavHostController): String {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return when (navBackStackEntry?.destination?.route) { // 檢查 mainNavController 的路由
        Routes.CHAT_LIST -> "聊天"
        Routes.CONTACTS -> "通訊錄"
        Routes.PROFILE -> "我的"
        else -> "JJLL" // 默認標題
    }
}