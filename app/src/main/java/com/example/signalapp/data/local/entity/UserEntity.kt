package com.example.signalapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users", // 定義表名
    indices = [Index(value = ["username"], unique = true)] // 為 username 創建唯一索引
)
data class UserEntity(
    // @PrimaryKey(autoGenerate = true) val id: Long = 0, // 如果需要自增主鍵 ID

    @PrimaryKey // 直接使用 username 作為主鍵，並保證唯一
    val username: String,

    // 注意：在生產環境中絕不應該直接存儲明文密碼！
    // 這裡為了簡化，暫時存儲 hash 或 representation
    // 實際應用中需要 hash 加鹽 (e.g., using bcrypt, Argon2)
    val passwordHash: String // 我們假設存儲的是密碼的 hash 值
    // 可以添加其他字段，如註冊時間戳等
    // val registrationTimestamp: Long = System.currentTimeMillis()
)