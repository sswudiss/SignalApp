package com.example.signalapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun ChatScreen(chatId: String) {
    val db = Firebase.firestore
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var newMessage by remember { mutableStateOf("") }
    val userId = Firebase.auth.currentUser?.uid ?: return

    // 讀取聊天訊息
    LaunchedEffect(chatId) {
        db.collection("chats").document(chatId).collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    messages = snapshot.documents.map { doc ->
                        Message(doc.getString("sender") ?: "", doc.getString("text") ?: "")
                    }
                }
            }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(messages) { msg ->
                ChatBubble(msg)
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            OutlinedTextField(
                value = newMessage,
                onValueChange = { newMessage = it },
                modifier = Modifier.weight(1f),
                label = { Text("輸入消息") }
            )

            Button(onClick = {
                if (newMessage.isNotEmpty()) {
                    val message = mapOf(
                        "sender" to userId,
                        "text" to newMessage,
                        "timestamp" to FieldValue.serverTimestamp()
                    )
                    db.collection("chats").document(chatId).collection("messages").add(message)
                    newMessage = ""
                }
            }) {
                Text("發送")
            }
        }
    }
}

@Composable
fun ChatBubble(message: Message) {
    val isMe = message.sender == Firebase.auth.currentUser?.uid
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        Text(
            message.text,
            modifier = Modifier
                .background(if (isMe) Color.Blue else Color.Gray, shape = RoundedCornerShape(8.dp))
                .padding(8.dp),
            color = Color.White
        )
    }
}

data class Message(val sender: String, val text: String)
