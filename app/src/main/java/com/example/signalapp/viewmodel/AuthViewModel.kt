package com.example.signalapp.viewmodel

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


fun registerUser(username: String, password: String, onSuccess: () -> Unit) {
    val auth = Firebase.auth
    val db = Firebase.firestore

    // 檢查用戶名是否已存在
    db.collection("users").whereEqualTo("username", username).get()
        .addOnSuccessListener { documents ->
            if (documents.isEmpty) {
                // 創建帳戶
                auth.createUserWithEmailAndPassword("$username@signal.com", password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val userId = task.result?.user?.uid ?: return@addOnCompleteListener
                            // 存儲用戶資訊
                            db.collection("users").document(userId).set(mapOf("username" to username))
                                .addOnSuccessListener { onSuccess() }
                        }
                    }
            } else {
                Log.e("Auth", "用戶名已存在")
            }
        }
}

fun loginUser(username: String, password: String, onSuccess: () -> Unit) {
    val auth = Firebase.auth
    auth.signInWithEmailAndPassword("$username@signal.com", password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onSuccess()
            } else {
                Log.e("Auth", "登入失敗")
            }
        }
}
