package com.example.signalapp.util


import org.mindrot.jbcrypt.BCrypt

object PasswordUtils {

    /**
     * 生成密碼的 BCrypt 哈希值。
     *
     * @param password 要哈希的明文密碼。
     * @param workload BCrypt 的工作因子 (推薦 10-12)，值越高越安全但越慢。
     *                 默認為 10。
     * @return BCrypt 哈希字符串 (包含了鹽)。
     */
    fun hashPassword(password: String, workload: Int = 10): String {
        // gensalt 會生成一個隨機鹽，workload 是計算強度
        val salt = BCrypt.gensalt(workload)
        // hashpw 會將鹽和密碼哈希組合在一起存儲
        return BCrypt.hashpw(password, salt)
    }

    /**
     * 驗證輸入的明文密碼是否與存儲的 BCrypt 哈希匹配。
     *
     * @param candidatePassword 用戶輸入的待驗證密碼。
     * @param storedHash 從數據庫讀取的 BCrypt 哈希字符串。
     * @return 如果密碼匹配返回 true，否則返回 false。
     */
    fun verifyPassword(candidatePassword: String, storedHash: String): Boolean {
        return try {
            // checkpw 會從 storedHash 中提取鹽，然後用相同的鹽哈希 candidatePassword，
            // 最後比較結果哈希是否一致。
            BCrypt.checkpw(candidatePassword, storedHash)
        } catch (e: IllegalArgumentException) {
            // 如果 storedHash 格式不正確，checkpw 可能拋出異常
            println("密碼驗證錯誤: 提供的哈希值格式無效。 ${e.message}")
            false
        } catch (e: Exception) {
            // 捕獲其他可能的異常
            println("密碼驗證時發生意外錯誤: ${e.message}")
            false
        }
    }
}