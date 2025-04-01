package com.example.signalapp


import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp // <--- 標記為 Hilt Application
class MyApplication : Application() {
    // 通常可以在這裡做一些全局初始化，但 Hilt 會處理很多
    override fun onCreate() {
        super.onCreate()
        // ... 其他初始化代碼
    }
}