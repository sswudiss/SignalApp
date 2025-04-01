package com.example.signalapp.ui.registration

/*
* 這個類將封裝註冊畫面需要的所有狀態。
* */
data class RegistrationUiState(
    val username: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val errorMessage: String? = null,
    val isLoading: Boolean = false,       // 新增：用於表示是否正在處理註冊
    val registrationSuccess: Boolean = false // 新增：用於標記註冊流程成功完成
)