package com.example.signalapp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.signalapp.model.Message

// *** 增加數據庫版本號！從 1 -> 2 ***
// 將 Contact 加入 entities 列表
abstract class AppDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao
    abstract fun contactDao(): ContactDao // 添加 ContactDao 的抽象方法

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // 定義遷移：從版本 1 到 版本 2 (創建 contacts 表)
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 執行 SQL 創建新的 contacts 表
                // 注意 SQL 語句的字段名和類型需要與 Contact 實體完全對應
                database.execSQL("CREATE TABLE IF NOT EXISTS `contacts` (`userId` TEXT NOT NULL, `username` TEXT NOT NULL, `displayName` TEXT, `photoUrl` TEXT, PRIMARY KEY(`userId`))")
                // 如果有需要，可以添加索引: database.execSQL("CREATE INDEX IF NOT EXISTS index_contacts_username ON contacts(username)")
            }
        }


        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jjll_database"
                )
                    // *** 添加遷移方案 ***
                    .addMigrations(MIGRATION_1_2) // 添加定義好的遷移
                    // 如果你仍然想在遷移失敗或沒寫遷移時丟棄數據 (僅開發時!)
                    // .fallbackToDestructiveMigration() // 保留這個或刪除，取決於策略
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

/*
* 數據庫版本升級: @Database 的 version 從 1 改為 2。
添加實體: entities 數組中加入了 Contact::class。
添加 DAO 方法: 增加了 abstract fun contactDao(): ContactDao。
添加遷移 (Migration): 因為我們更改了數據庫結構（增加了 contacts 表），
* 必須告訴 Room 如何從舊版本 (1) 過渡到新版本 (2)。我們定義了 MIGRATION_1_2，
* 其中使用 SQL 語句創建了新的 contacts 表。並在 databaseBuilder 中通過
* .addMigrations(MIGRATION_1_2) 添加了這個遷移。
* 如果不提供遷移且不使用 fallbackToDestructiveMigration，應用會崩潰！
* */