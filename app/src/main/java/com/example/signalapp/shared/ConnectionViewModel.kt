package com.example.signalapp.shared

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ConnectionViewModel : ViewModel() {

    // 錯誤消息狀態，String? 表示可以沒有錯誤 (null) 或有具體的錯誤信息
    var errorMessage by mutableStateOf<String?>(null)
        private set

    private var autoClearJob: Job? = null

    // 供其他 ViewModel 或 Repositories 調用以顯示錯誤
    fun showConnectionError(message: String = "連接服務器失敗，請檢查網絡") {
        errorMessage = message
        // 可以選擇在一段時間後自動清除錯誤提示
        autoClearJob?.cancel() // 取消之前的自動清除任務
        autoClearJob = viewModelScope.launch {
            delay(5000L) // 顯示 5 秒
            clearConnectionError()
        }
    }

    // 清除錯誤消息
    fun clearConnectionError() {
        errorMessage = null
        autoClearJob?.cancel() // 如果手動清除，也取消自動清除任務
    }

    // 當 ViewModel 清除時，確保取消協程
    override fun onCleared() {
        super.onCleared()
        autoClearJob?.cancel()
    }
}