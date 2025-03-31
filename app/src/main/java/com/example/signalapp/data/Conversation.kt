package com.example.signalapp.data

data class Conversation(
    val id: String,
    val name: String, // 顯示在列表中的名字
    val lastMessage: String // 列表預覽的最後訊息
)