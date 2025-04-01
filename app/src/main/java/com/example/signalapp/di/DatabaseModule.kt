package com.example.signalapp.di

import android.content.Context
import androidx.room.Room
import com.example.signalapp.data.local.AppDatabase
import com.example.signalapp.data.local.dao.ChatDao
import com.example.signalapp.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext // 獲取 Application Context
import dagger.hilt.components.SingletonComponent   // 指定組件生命週期 (Application)
import javax.inject.Singleton

//新的包 di (Dependency Injection)

@Module
@InstallIn(SingletonComponent::class) // 安裝到 Application 生命週期的組件中
object DatabaseModule {

    @Provides
    @Singleton // 確保數據庫只有一個實例
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "signal_app_database" // 數據庫名
        )
            //.fallbackToDestructiveMigration() // 謹慎使用
            .build()
    }

    @Provides // Hilt 會知道 AppDatabase 來自上面的 provideAppDatabase
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
        // DAO 本身不需要是 @Singleton，因為 AppDatabase 已經是了
    }

    // --- 將來添加其他 DAO 的 Provides 方法 ---
    // @Provides fun provideChatDao(database: AppDatabase): ChatDao { ... }
    @Provides
    fun provideChatDao(database: AppDatabase): ChatDao {
        return database.chatDao()
    }
}