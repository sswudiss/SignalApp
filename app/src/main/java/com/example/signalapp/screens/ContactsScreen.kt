package com.example.signalapp.screens


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.signalapp.data.Contact
import com.example.signalapp.data.DummyDataProvider
import com.example.signalapp.R

@Composable
fun ContactsScreen() {
    val contacts = DummyDataProvider.contactList // 獲取假數據

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(contacts, key = { contact -> contact.id }) { contact ->
            ContactItem(contact = contact, onClick = {
                // TODO: 處理點擊事件，例如打開聊天或查看聯繫人詳情
                println("Clicked on contact: ${contact.name}")
            })
        }
    }
    // TODO: 可以在 Scaffold 中添加 FAB 或按鈕用於添加聯繫人
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactItem(
    contact: Contact,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = {
            Text(
                text = contact.name,
                fontWeight = FontWeight.Normal, // 普通字重
                fontSize = 16.sp
            )
        },
        leadingContent = {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(contact.avatarUrl)
                    .crossfade(true)
                    .placeholder(R.drawable.ic_launcher_foreground) // 需要佔位符
                    .error(R.drawable.ic_launcher_foreground) // 需要佔位符
                    .build(),
                contentDescription = "${contact.name} avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            )
            // Icon(
            //     imageVector = Icons.Default.AccountCircle,
            //     contentDescription = "${contact.name} avatar",
            //     modifier = Modifier.size(40.dp),
            //     tint = MaterialTheme.colorScheme.primary
            // )
        },
        tonalElevation = 1.dp
    )
}
