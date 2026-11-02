package com.example.temolink.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.temolink.data.model.MessageThread
import com.example.temolink.ui.theme.*
import com.example.temolink.ui.viewmodel.AuthViewModel
import com.example.temolink.ui.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesListScreen(
    onNavigateToChat: (String, String) -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    chatViewModel: ChatViewModel = viewModel()
) {
    val user by authViewModel.currentUser.collectAsState()
    val threads by chatViewModel.threads.collectAsState()

    LaunchedEffect(user) {
        user?.let {
            chatViewModel.loadThreads(it.uid, it.role.name)
        }
    }

    Scaffold(
        containerColor = DarkNavy,
        topBar = {
            TopAppBar(
                title = { Text("Messages", color = PrimaryBlue, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavy)
            )
        }
    ) { paddingValues ->
        if (threads.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No messages yet.", color = SecondaryText)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(threads) { thread ->
                    ThreadItem(
                        thread = thread,
                        currentUserId = user?.uid ?: "",
                        onClick = {
                            val otherId = if (user?.role?.name == "FARMER") thread.buyerId else thread.farmerId
                            onNavigateToChat(thread.threadId, otherId)
                        }
                    )
                    HorizontalDivider(color = PanelBlue, thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
fun ThreadItem(thread: MessageThread, currentUserId: String, onClick: () -> Unit) {
    val otherName = if (thread.buyerId == currentUserId) thread.farmerName else thread.buyerName
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val isUnread = thread.lastMessageSenderId != currentUserId && !thread.lastMessageRead && thread.lastMessage.isNotEmpty()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(PanelBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = MutedText)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = otherName,
                    color = PrimaryText,
                    fontWeight = if (isUnread) FontWeight.ExtraBold else FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isUnread) {
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(10.dp)
                                .background(Color.Red, CircleShape)
                        )
                    }
                    Text(
                        text = dateFormat.format(Date(thread.lastMessageAt)),
                        color = if (isUnread) PrimaryBlue else MutedText,
                        fontSize = 12.sp,
                        fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = thread.lastMessage.ifEmpty { "Start a conversation" },
                color = if (isUnread) PrimaryText else if (thread.lastMessage.isEmpty()) MutedText else SecondaryText,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = if (isUnread) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}
