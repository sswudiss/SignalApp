package com.example.signalapp.data

data class ChatPreview(
    val id: String,
    val contactName: String,
    val lastMessage: String,
    val timestamp: String, // 暫時用 String
    val avatarUrl: String? = null // 可選頭像 URL
)

data class Contact(
    val id: String,
    val name: String,
    val avatarUrl: String? = null
)

object DummyDataProvider {

    val chatList = listOf(
        ChatPreview("1", "Alice", "好的，明天見！", "10:30 AM"),
        ChatPreview("2", "Bob", "你收到文件了嗎？", "昨天"),
        ChatPreview("3", "開發團隊", "版本已發布。", "星期二"),
        ChatPreview("4", "Charlie", "哈哈哈哈 😂", "11/05/2024"),
        ChatPreview("5", "家庭群組", "晚上吃什麼？", "11/05/2024"),
        ChatPreview("6", "David", "在路上了", "10/05/2024"),
    )

    val contactList = listOf(
        Contact("1", "Alice"),
        Contact("2", "Bob"),
        Contact("3", "Charlie"),
        Contact("4", "David"),
        Contact("5", "Eve"),
        Contact("6", "Frank"),
        Contact("7", "Grace"),
        Contact("8", "Heidi"),
    )
}