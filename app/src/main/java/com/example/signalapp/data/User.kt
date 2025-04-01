package com.example.signalapp.data


data class User(
    val id: String, // 用戶唯一 ID
    val username: String,
    val avatarUrl: String? = null // 可選頭像
)