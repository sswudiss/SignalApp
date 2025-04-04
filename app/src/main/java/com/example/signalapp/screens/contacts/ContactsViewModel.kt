package com.example.signalapp.screens.contacts

import com.example.signalapp.db.ContactDao
import com.example.signalapp.model.Contact
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactDao: ContactDao
) : ViewModel() {

    // 從 DAO 獲取聯繫人列表 Flow，並轉換為 StateFlow 供 UI 觀察
    val contacts: StateFlow<List<Contact>> = contactDao.getAllContacts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // UI 可見時激活
            initialValue = emptyList() // 初始為空列表
        )

    // --- 用於階段一測試的【手動添加】功能 ---
    fun addContactManually(userId: String, username: String, displayName: String?) {
        if (userId.isBlank() || username.isBlank()) return
        viewModelScope.launch {
            val newContact = Contact(
                userId = userId,
                username = username,
                displayName = displayName ?: username, // 如果沒有顯示名，用用戶名代替
                photoUrl = null // 暫無頭像
            )
            contactDao.insertContact(newContact)
        }
    }

    // --- 搜索相關（階段二實現）---
    // var searchQuery by mutableStateOf("")
    // var searchResults by mutableStateOf<List<FoundUser>>(emptyList())
    // var isSearching by mutableStateOf(false)
    // fun searchUsers(query: String) { /* ... Firestore 查詢 ... */ }
    // fun addContactFromSearch(foundUser: FoundUser) { /* ... 調用 contactDao.insertContact ... */ }

    // --- 刪除聯繫人 (可以現在添加邏輯) ---
    fun deleteContact(contact: Contact) {
        viewModelScope.launch {
            contactDao.deleteContact(contact)
        }
    }
}