package com.example.signalapp.model


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class Contact(
    // 使用 Firebase 返回的 User ID 作為主鍵
    @PrimaryKey
    val userId: String,

    // 我們需要顯示的用戶名 (用戶註冊時使用的，唯一)
    val username: String,

    // 可以存儲顯示名稱（如果用戶設置了displayName），方便顯示
    val displayName: String?,

    // 可以存儲頭像 URL，方便顯示
    val photoUrl: String?
    // 未來可以添加字段，如 isBlocked, notes 等
)