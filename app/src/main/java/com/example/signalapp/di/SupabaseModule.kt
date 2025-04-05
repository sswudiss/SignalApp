package com.example.signalapp.di


import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient         // <--- 確認這個 import 是否還有紅線? 如果有，說明依賴問題未解決
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue          // Auth 插件
import io.github.jan.supabase.postgrest.Postgrest     // Database 插件
import io.github.jan.supabase.realtime.Realtime       // Realtime 插件
import io.github.jan.supabase.storage.Storage         // Storage 插件
import io.ktor.client.engine.cio.CIO                // <--- 你選擇的 Ktor 引擎 (或者 Android)
// import io.ktor.client.engine.android.Android     // <--- 或者選擇這個引擎
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // 依賴在 Application 生命週期內有效 (單例)
object SupabaseModule {

    // --- 重要：在此處替換為你的 Supabase 項目信息 ---
    // 你可以在 Supabase 項目的 Settings -> API 頁面找到這些值
    private const val SUPABASE_URL =
        "https://egqabugxkpnhvkeuarys.supabase.co" // <-- 替換 <your-project-ref>
    private const val SUPABASE_ANON_KEY =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVncWFidWd4a3BuaHZrZXVhcnlzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDM4NDYzNTcsImV4cCI6MjA1OTQyMjM1N30.xtF_0nR2hDXrqVWOOb1JJ_D3oRRzlk9aMB8SzboUG6Y" // <-- 替換 <your-anon-key>

    @Provides
    @Singleton // 確保 SupabaseClient 只創建一次 (單例)
    fun provideSupabaseClient(): SupabaseClient {

        // 1. 基本配置檢查 (開發階段有助於快速發現問題)
        require(!SUPABASE_URL.contains("https://egqabugxkpnhvkeuarys.supabase.co") && SUPABASE_URL.isNotBlank()) {
            "Supabase URL is not configured correctly in SupabaseModule.kt. Please replace placeholder."
        }
        require(!SUPABASE_ANON_KEY.contains("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVncWFidWd4a3BuaHZrZXVhcnlzIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDM4NDYzNTcsImV4cCI6MjA1OTQyMjM1N30.xtF_0nR2hDXrqVWOOb1JJ_D3oRRzlk9aMB8SzboUG6Y") && SUPABASE_ANON_KEY.isNotBlank()) {
            "Supabase Anon Key is not configured correctly in SupabaseModule.kt. Please replace placeholder."
        }

        // 2. 創建 SupabaseClient 實例
        return createSupabaseClient(
            supabaseUrl = SUPABASE_URL,
            supabaseKey = SUPABASE_ANON_KEY
        ) {
            // 3. 安裝你需要的 Supabase 插件
            install(GoTrue) { // 安裝身份驗證插件
                // 在這裡可以添加 GoTrue 特定的配置，例如：
                autoLoadFromStorage = true // 從本地加載會話信息
                alwaysAutoRefresh = true   // 自動刷新 Auth Token
                // scheme = "your.custom.scheme" // 如果需要處理 OAuth 重定向的 Deep Link
                // host = "your.custom.host"
            }

            install(Postgrest) { // 安裝數據庫交互插件
                // Postgrest 配置 (通常用默認值即可)
                // defaultSchema = "public"
            }

            install(Realtime) { // 安裝實時數據訂閱插件
                // Realtime 配置
                // reconnectDelay = kotlin.time.Duration.Companion.seconds(5) // 例如，設置重連延遲
            }

            install(Storage) { // 安裝文件存儲插件
                // Storage 配置
            }

            // 4. **必須** 配置 Ktor HTTP Client Engine
            // 根據你在 build.gradle.kts 中添加的依賴，選擇並取消註釋下面的一行

            // 使用 CIO 引擎 (需要 'io.ktor:ktor-client-cio:...')
            httpEngine = CIO.create {
                // 可選的 CIO 引擎配置
                // requestTimeout = 15000 // 例如，設置請求超時 (毫秒)
            }

            // 或者使用 Android 引擎 (需要 'io.ktor:ktor-client-android:...')
            // httpEngine = Android.create {
            //     // 可選的 Android 引擎配置
            //     // connectTimeout = 15000
            //     // socketTimeout = 15000
            // }

            // 注意：不能同時啟用兩個引擎！
        }
    }
}