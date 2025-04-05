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
import android.util.Log // 導入 Log
import androidx.compose.runtime.* // 導入 mutableStateOf 等
import com.example.signalapp.model.FoundUser
import com.google.firebase.auth.FirebaseAuth // 導入 FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore // 導入 Firestore
import com.google.firebase.firestore.ktx.firestore // 導入 KTX
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.* // 確保導入 flow operators
import kotlinx.coroutines.tasks.await


@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactDao: ContactDao
) : ViewModel() {

    private val firestore: FirebaseFirestore = Firebase.firestore // Firestore 實例
    private val currentUser = FirebaseAuth.getInstance().currentUser // 當前登錄用戶

    // --- 本地聯繫人列表 ---
    val contacts: StateFlow<List<Contact>> = contactDao.getAllContacts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // --- 搜索相關狀態 ---
    var searchQuery by mutableStateOf("")
        private set
    var searchResults by mutableStateOf<List<FoundUser>>(emptyList())
        private set
    var isSearching by mutableStateOf(false)
        private set
    var searchErrorMessage by mutableStateOf<String?>(null)
        private set

    // --- 搜索邏輯 ---
    fun onSearchQueryChange(query: String) {
        searchQuery = query
        // 可以添加 debounce 來避免過於頻繁的搜索請求 (需要 FlowPreview)
        // 或者在用戶停止輸入後一段時間再搜索
        if (query.isNotBlank() && query.length >= 2) { // 至少輸入2個字符才搜索
            searchUsers(query)
        } else {
            searchResults = emptyList() // 清空結果
            isSearching = false
            searchErrorMessage = null
        }
    }

    private fun searchUsers(query: String) {
        isSearching = true
        searchErrorMessage = null
        searchResults = emptyList() // 清空上次結果

        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("users")
                    // 使用 >= 和 <= 實現前綴匹配搜索 (比精確匹配更友好)
                    // 注意：這需要為 username 字段在 "集合組" (Collection Group) 上創建索引
                    .whereGreaterThanOrEqualTo("username", query.trim())
                    .whereLessThanOrEqualTo("username", query.trim() + '\uf8ff') // \uf8ff 是 Unicode 中很高的碼點
                    // 或者使用精確匹配: .whereEqualTo("username", query.trim())
                    .limit(10) // 限制結果數量
                    .get()
                    .await()

                val foundUsers = snapshot.documents.mapNotNull { doc ->
                    // 轉換 Firestore 文檔為 FoundUser 對象
                    FoundUser(
                        userId = doc.id,
                        username = doc.getString("username") ?: "",
                        displayName = doc.getString("displayName"),
                        photoUrl = doc.getString("photoUrl")
                    )
                }.filter {
                    // 過濾掉自己
                    it.userId != currentUser?.uid
                }

                searchResults = foundUsers
                if(foundUsers.isEmpty() && searchQuery.isNotBlank()){ // 只有在確實搜索了且無結果時才提示
                    searchErrorMessage = "未找到用戶 '$query'"
                }


            } catch (e: Exception) {
                Log.e("ContactsViewModel", "Error searching users", e)
                searchErrorMessage = "搜索時發生錯誤: ${e.localizedMessage}"
                searchResults = emptyList() // 清空結果
            } finally {
                isSearching = false
            }
        }
    }

    // --- 添加聯繫人 ---
    fun addContactFromSearch(foundUser: FoundUser) {
        viewModelScope.launch {
            try {
                // 檢查是否已是聯繫人 (雖然 INSERT REPLACE 會處理，但可以提前避免)
                // if (contactDao.getContactById(foundUser.userId) != null) {
                //     println("Contact already exists: ${foundUser.username}")
                //     return@launch
                // }

                val newContact = Contact(
                    userId = foundUser.userId,
                    username = foundUser.username,
                    displayName = foundUser.displayName ?: foundUser.username, // 優先用 displayName
                    photoUrl = foundUser.photoUrl
                )
                contactDao.insertContact(newContact)
                // 添加成功後可以清空搜索，或給用戶提示
                clearSearch() // 添加後清空搜索
                Log.d("ContactsViewModel", "Contact added: ${newContact.username}")
            } catch (e: Exception) {
                Log.e("ContactsViewModel", "Error adding contact", e)
                // TODO: 顯示錯誤給用戶
            }
        }
    }

    // 清空搜索
    fun clearSearch() {
        searchQuery = ""
        searchResults = emptyList()
        searchErrorMessage = null
        isSearching = false
    }


    // --- 刪除聯繫人 (保持不變) ---
    fun deleteContact(contact: Contact) { /* ... */ }

    // --- 移除手動添加功能 ---
    // fun addContactManually(...) { /* 可以移除或註釋掉了 */ }
}