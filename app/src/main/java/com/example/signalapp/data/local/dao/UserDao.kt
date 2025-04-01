package com.example.signalapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.signalapp.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface UserDao {

    /**
     * 根據用戶名查找用戶。
     * 使用 suspend 關鍵字表示這是一個掛起函數，應在協程中調用。
     * @param username 要查找的用戶名
     * @return 找到的 UserEntity，如果不存在則返回 null。
     */
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    /**
     * 插入一個新用戶。
     * 如果用戶名已存在，則替換舊記錄 (也可以選 ABORT, IGNORE 等策略)。
     * 使用 OnConflictStrategy.REPLACE 可能會覆蓋密碼，需謹慎。
     * 對於註冊，如果用戶存在應報錯，所以 ABORT 可能更好，但需要 try-catch。
     * 為了簡化 ViewModel 邏輯，先用 REPLACE，檢查應在 ViewModel 中進行。
     * @param user 要插入的用戶實體
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE) // 或者 ABORT, IGNORE
    suspend fun insertUser(user: UserEntity)

    /**
     * 檢查用戶名是否存在 (優化查詢，只返回是否存在)。
     * @param username 要檢查的用戶名
     * @return 如果用戶名存在返回 true，否則返回 false。
     */
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE username = :username LIMIT 1)")
    suspend fun doesUserExist(username: String): Boolean

    /**
     * 獲取所有用戶，可以排除某個用戶。
     * 返回 Flow 以便列表動態更新。
     * @param excludeUsername 要排除的用戶名 (例如當前登錄用戶)
     */
    @Query("SELECT * FROM users WHERE username != :excludeUsername ORDER BY username ASC")
    fun getAllOtherUsers(excludeUsername: String): Flow<List<UserEntity>> // 返回 Flow
}