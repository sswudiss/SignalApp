package com.example.signalapp

object AppDestinations {
    const val CONVERSATION_LIST_ROUTE = "conversationList"
    // 注意這裡的語法：我們定義了基礎路由和參數名稱
    const val CHAT_ROUTE = "chat"
    const val CHAT_ID_KEY = "conversationId" // 參數的 key
    // 完整的聊天畫面路由模板 (用於 NavHost)
    val chatRouteWithArgs = "$CHAT_ROUTE/{$CHAT_ID_KEY}"
}