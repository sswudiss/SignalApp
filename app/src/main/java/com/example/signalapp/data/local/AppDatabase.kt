package com.example.signalapp.data.local


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.signalapp.data.local.dao.ChatDao // <--- 導入新的 DAO
import com.example.signalapp.data.local.dao.UserDao
import com.example.signalapp.data.local.entity.ChatMessageEntity
import com.example.signalapp.data.local.entity.ChatSummaryEntity
import com.example.signalapp.data.local.entity.UserEntity


@Database(
    entities = [
        UserEntity::class,
        ChatSummaryEntity::class, // <--- 添加 ChatSummaryEntity
        ChatMessageEntity::class  // <--- 添加 ChatMessageEntity
    ],
    version = 2, // <<<--- 版本號增加到 2
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun chatDao(): ChatDao // <--- 添加 ChatDao 抽象方法

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "signal_app_database"
                )
                    // --- !!! 重要警告 !!! ---
                    // 因為我們更改了數據庫結構 (增加了表，版本從 1 到 2)
                    // 我們需要告訴 Room 如何處理舊版本。
                    // fallbackToDestructiveMigration() 會在版本不匹配時
                    // 直接刪除舊數據庫並重新創建，所有數據都會丟失！
                    // 這在開發初期很方便，但在生產環境中絕對不能這樣做。
                    // 生產環境需要使用 .addMigrations(...) 提供詳細的遷移步驟。
                    .fallbackToDestructiveMigration() // <-- 容易導致數據丟失！
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}