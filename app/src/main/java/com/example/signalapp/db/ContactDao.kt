package com.example.signalapp.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import com.example.signalapp.model.Contact
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE) // 如果已添加，則替換（更新信息）
    suspend fun insertContact(contact: Contact)

    @Query("SELECT * FROM contacts ORDER BY displayName ASC, username ASC") // 按顯示名稱或用戶名排序
    fun getAllContacts(): Flow<List<Contact>> // 返回 Flow 以便觀察變化

    @Query("SELECT * FROM contacts WHERE userId = :userId LIMIT 1")
    suspend fun getContactById(userId: String): Contact? // 查找特定聯繫人

    @Delete
    suspend fun deleteContact(contact: Contact) // 刪除聯繫人 (可以通過對象或 ID)

    @Query("DELETE FROM contacts WHERE userId = :userId")
    suspend fun deleteContactById(userId: String) // 通過 ID 刪除聯繫人
}