package com.example.signalapp.navigation

/*
* CHAT_LIST_ROUTE 代表聊天列表頁面。
* REGISTRATION_ROUTE 代表註冊頁面。
* */
object AppDestinations {
    const val REGISTRATION_ROUTE = "registration"
    const val CHAT_LIST_ROUTE = "chat_list"

    const val CHAT_DETAIL_ROUTE = "chatDetail" // 基礎路由名稱
    const val NEW_CHAT_ROUTE = "newChat"

    // 定義聊天詳情頁的完整路由模式，包含參數 chatId
    const val CHAT_DETAIL_PATH = "$CHAT_DETAIL_ROUTE/{chatId}"

    // Helper function to create the navigation path easily
    fun chatDetailPath(chatId: String) = "$CHAT_DETAIL_ROUTE/$chatId"
}

// 路由參數名稱的常量 (好習慣)
object NavigationArgs {
    const val CHAT_ID = "chatId"
}


/*
* 我們定義了 CHAT_DETAIL_ROUTE 作為基礎名稱，CHAT_DETAIL_PATH 作為包含 {chatId} 佔位符的完整路徑模式。
添加了一個輔助函數 chatDetailPath(chatId)，讓導航時構建路徑更安全方便。
NavigationArgs.CHAT_ID 用於在定義和提取參數時保持一致性。
* */