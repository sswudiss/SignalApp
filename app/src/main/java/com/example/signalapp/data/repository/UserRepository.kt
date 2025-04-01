package com.example.signalapp.data.repository

import com.example.signalapp.data.local.dao.UserDao
import com.example.signalapp.data.local.entity.UserEntity
import com.example.signalapp.util.PasswordUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

// 實際應用中可能還需要加密庫的依賴

/**
 * UserRepository 作為 User 相關數據的操作入口。
 * 它封裝了數據來源 (這裡只有 UserDao，將來可能有網絡 API)。
 */
@Singleton // 可選：將 Repository 標記為單例，Hilt 會確保只創建一個實例
class UserRepository @Inject constructor( // <--- 添加 @Inject
    private val userDao: UserDao
) {
    /**
     * 檢查用戶名是否存在。
     */
    suspend fun isUsernameTaken(username: String): Boolean {
        // Room 的 suspend DAO 方法已經在後台線程執行，
        // 但如果 Repository 有更複雜邏輯或組合多個數據源，
        // 顯式使用 withContext(Dispatchers.IO) 是個好習慣。
        return withContext(Dispatchers.IO) {
            userDao.doesUserExist(username)
        }
    }

    /**
     * 註冊新用戶。
     * @param username 用戶名
     * @param password 明文密碼 (應在傳遞給此方法前處理或在此方法內處理)
     * @return 如果註冊成功返回 true，如果用戶名已存在返回 false。
     *         在實際應用中可以返回更豐富的結果，例如 Result<Unit, RegistrationError>
     */
    suspend fun registerUser(username: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            // 1. 先檢查用戶名是否已存在 (避免依賴 Insert 的 OnConflict)
            if (userDao.doesUserExist(username)) {
                return@withContext false // 用戶名已存在，註冊失敗
            }

            // --- 重要：密碼哈希 ---
            // 在這裡實現密碼哈希邏輯
            val passwordHash = PasswordUtils.hashPassword(password) // 生成真實哈希
            val newUser = UserEntity(username = username, passwordHash = passwordHash) // 使用哈希值

            try {
                // 插入數據庫
                userDao.insertUser(newUser)
                return@withContext true // 插入成功
            } catch (e: Exception) {
                // 處理可能的數據庫插入異常 (例如，儘管檢查了，但還是出現了並發衝突)
                println("註冊用戶時出錯: ${e.message}")
                // 可以根據異常類型判斷是否是約束衝突
                return@withContext false // 插入失敗
            }
        }
    }

    /**
     * 驗證用戶名和密碼 (用於登錄，目前我們沒有登錄流程)。
     * @param username 用戶名
     * @param password 輸入的明文密碼
     * @return 如果驗證成功返回 true，否則返回 false。
     */
    suspend fun validateUser(username: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            val user = userDao.getUserByUsername(username) ?: return@withContext false // 用戶不存在

            // --- 重要：密碼驗證 ---
            // 在這裡比較輸入密碼的哈希值和存儲的哈希值
            return@withContext PasswordUtils.verifyPassword(password, user.passwordHash) // 比較真實哈希
        }
    }

    /**
     * 獲取除指定用戶外的所有其他註冊用戶列表的 Flow。
     * @param currentUsername 當前登錄的用戶名，將被排除在列表外。
     */
    fun getOtherUsersStream(currentUsername: String): Flow<List<UserEntity>> {
        return userDao.getAllOtherUsers(currentUsername)
        // 注意：返回的是 Entity，可以在 ViewModel 或 UseCase 中映射為 UI Model
    }
}