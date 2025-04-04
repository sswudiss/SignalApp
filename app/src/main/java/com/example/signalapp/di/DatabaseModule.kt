package com.example.signalapp.di


import android.content.Context
import androidx.room.Room
import com.example.signalapp.db.AppDatabase
import com.example.signalapp.db.ContactDao
import com.example.signalapp.db.MessageDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext // 導入 @ApplicationContext
import dagger.hilt.components.SingletonComponent // 導入組件範圍
import javax.inject.Singleton // 導入 @Singleton


//Hilt Module 告訴 Hilt 如何創建那些無法通過構造函數注入的類的實例（例如 Room 數據庫、Retrofit 接口、第三方庫等）。

@Module
@InstallIn(SingletonComponent::class) // 表示這個 Module 中的綁定在 Application 的生命週期內有效 (單例)
object DatabaseModule {

    @Provides
    @Singleton // 確保數據庫實例是單例
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        // Hilt 會自動注入 Application Context
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "jjll_database" // 數據庫文件名
        )
            .fallbackToDestructiveMigration() // 保留遷移策略 (或替換為實際的 Migration)
            .build()
    }

    @Provides
    // 不需要 @Singleton，因為 AppDatabase 是 Singleton，每次從它獲取 DAO 都是同一個數據庫實例
    fun provideMessageDao(appDatabase: AppDatabase): MessageDao {
        // Hilt 會自動從上面的 provideAppDatabase 方法獲取 AppDatabase 實例
        return appDatabase.messageDao()
    }

    @Provides
    fun provideContactDao(appDatabase: AppDatabase): ContactDao {
        return appDatabase.contactDao() // 從 AppDatabase 實例獲取 ContactDao
    }
}

/*
* @Module: 標註這是一個 Hilt Module。

@InstallIn(SingletonComponent::class): 指定這個 Module 中的依賴提供方法將安裝在 SingletonComponent 中，意味著它們提供的實例將是應用程式範圍的單例。
@Provides: 標註函數，告訴 Hilt 如何創建某個類型的實例。
@Singleton: 用於標註 @Provides 函數，確保 Hilt 只創建一次該依賴的實例（僅對 SingletonComponent 有意義）。數據庫通常是單例。
@ApplicationContext: Hilt 提供的註解，可以直接注入 Application 的 Context。
provideAppDatabase: 這個函數告訴 Hilt 如何創建 AppDatabase。它接收 Hilt 自動提供的 @ApplicationContext。
provideMessageDao: 這個函數告訴 Hilt 如何創建 MessageDao。它接收一個 AppDatabase 參數，Hilt 會自動調用 provideAppDatabase 來獲取這個參數的值，然後返回 appDatabase.messageDao()。
* */