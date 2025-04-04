package com.example.signalapp


import android.app.Application
import com.example.signalapp.db.AppDatabase
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp // 添加 Hilt 註解
class JJLLApplication : Application() {

    // --- 不再需要直接暴露數據庫實例 ---
    // 使用 lazy 委託確保數據庫只在首次訪問時創建
   // val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        // 可以在這裡執行其他全局初始化操作
        // Hilt 會在這裡進行初始化
    }
}

/*
* 添加 @HiltAndroidApp 註解。
移除或註釋掉之前暴露 database 實例的代碼。Hilt 將負責數據庫實例的創建和提供。
* */