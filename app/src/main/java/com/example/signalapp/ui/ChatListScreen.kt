package com.example.signalapp.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun ChatListScreen(navController: NavController) {
    val userId = Firebase.auth.currentUser?.uid ?: return
    val db = Firebase.firestore
    var chatRooms by remember { mutableStateOf<List<ChatRoom>>(emptyList()) }

    // 讀取聊天室列表
    LaunchedEffect(Unit) {
        db.collection("chats").whereArrayContains("users", userId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    chatRooms = snapshot.documents.map { doc ->
                        ChatRoom(doc.id, doc.getString("lastMessage") ?: "")
                    }
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("聊天室", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        LazyColumn {
            items(chatRooms) { chat ->
                ChatRoomItem(chat) {
                    navController.navigate("chat/${chat.id}")
                }
            }
        }
    }
}

@Composable
fun ChatRoomItem(chat: ChatRoom, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp).clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(chat.lastMessage, modifier = Modifier.weight(1f))
        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "進入聊天")
    }
}

data class ChatRoom(val id: String, val lastMessage: String)
