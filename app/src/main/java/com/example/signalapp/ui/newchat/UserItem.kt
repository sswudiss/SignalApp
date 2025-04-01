package com.example.signalapp.ui.newchat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person // Placeholder icon
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.signalapp.data.User
import com.example.signalapp.ui.theme.SignalAppTheme

@Composable
fun UserItem(
    user: User,
    onClick: (String) -> Unit, // 點擊時傳回 userID
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(user.id) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 頭像佔位符
        Image(
            imageVector = Icons.Default.Person,
            contentDescription = "${user.username} 頭像",
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer)
        )
        // TODO: 使用 Coil/Glide 加載真實頭像 url

        Spacer(modifier = Modifier.width(16.dp))

        // 用戶名
        Text(
            text = user.username,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UserItemPreview() {
    val previewUser = User("preview_id", "預覽用戶名")
    SignalAppTheme {
        UserItem(user = previewUser, onClick = {})
    }
}