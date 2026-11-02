package com.example.temolink.ui.screens.chat

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.temolink.data.model.ChatMessage
import com.example.temolink.ui.theme.*
import com.example.temolink.ui.viewmodel.AuthViewModel
import com.example.temolink.ui.viewmodel.ChatViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ChatScreen(
    threadId: String,
    otherUserId: String,
    onBack: () -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    chatViewModel: ChatViewModel = viewModel()
) {
    val user by authViewModel.currentUser.collectAsState()
    val messages by chatViewModel.messages.collectAsState()
    var messageText by remember { mutableStateOf("") }
    
    var editingMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var messageToDelete by remember { mutableStateOf<ChatMessage?>(null) }
    
    val listState = rememberLazyListState()

    LaunchedEffect(threadId) {
        chatViewModel.loadMessages(threadId)
    }

    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
            
            // Mark all unread incoming messages as read
            messages.forEach { msg ->
                if (msg.senderId != user?.uid && !msg.read) {
                    chatViewModel.markAsRead(msg.messageId)
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (messageToDelete != null) {
        AlertDialog(
            onDismissRequest = { messageToDelete = null },
            title = { Text("Delete Message", color = PrimaryText) },
            text = { Text("Are you sure you want to delete this message? This action cannot be undone.", color = SecondaryText) },
            confirmButton = {
                TextButton(
                    onClick = {
                        messageToDelete?.let { chatViewModel.deleteMessage(it.messageId) }
                        messageToDelete = null
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { messageToDelete = null }) {
                    Text("Cancel", color = PrimaryBlue)
                }
            },
            containerColor = PanelBlue
        )
    }

    Scaffold(
        containerColor = DarkNavy,
        topBar = {
            TopAppBar(
                title = { Text("Chat", color = PrimaryBlue, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PrimaryText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavy)
            )
        },
        bottomBar = {
            Surface(
                color = SecondaryNavy,
                modifier = Modifier.imePadding()
            ) {
                Column {
                    if (editingMessage != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Editing message", color = PrimaryBlue, fontSize = 12.sp)
                            }
                            IconButton(onClick = { 
                                editingMessage = null
                                messageText = ""
                            }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel", tint = MutedText)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Type a message...", color = MutedText) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryBlue,
                                unfocusedBorderColor = PanelBlue,
                                focusedTextColor = PrimaryText,
                                unfocusedTextColor = PrimaryText,
                                focusedContainerColor = DarkNavy,
                                unfocusedContainerColor = DarkNavy
                            ),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 4
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                user?.let {
                                    if (editingMessage != null) {
                                        chatViewModel.editMessage(editingMessage!!.messageId, messageText)
                                        editingMessage = null
                                    } else {
                                        chatViewModel.sendMessage(threadId, it.uid, otherUserId, messageText)
                                    }
                                    messageText = ""
                                }
                            },
                            enabled = messageText.isNotBlank()
                        ) {
                            Icon(
                                if (editingMessage != null) Icons.Default.Check else Icons.AutoMirrored.Filled.Send, 
                                contentDescription = if (editingMessage != null) "Save" else "Send", 
                                tint = PrimaryBlue
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            items(messages, key = { it.messageId }) { message ->
                MessageBubble(
                    message = message,
                    isMe = message.senderId == user?.uid,
                    onDelete = { 
                        messageToDelete = message
                    },
                    onEdit = { 
                        editingMessage = message
                        messageText = message.text
                    }
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: ChatMessage, 
    isMe: Boolean,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    var showMenu by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Box {
            Surface(
                color = if (isMe) PrimaryBlue else PanelBlue,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isMe) 16.dp else 0.dp,
                    bottomEnd = if (isMe) 0.dp else 16.dp
                ),
                modifier = Modifier.combinedClickable(
                    onClick = { },
                    onLongClick = { if (isMe) showMenu = true }
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = message.text,
                        color = if (isMe) Color.White else PrimaryText,
                        fontSize = 15.sp
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        if (message.edited) {
                            Text(
                                text = "edited",
                                color = (if (isMe) Color.White else MutedText).copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                        
                        Text(
                            text = dateFormat.format(Date(message.createdAt)),
                            color = if (isMe) Color.White.copy(alpha = 0.7f) else MutedText,
                            fontSize = 10.sp
                        )
                        
                        if (isMe) {
                            Spacer(modifier = Modifier.width(4.dp))
                            val tickColor = if (message.read) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.7f)
                            val icon = if (message.read || message.delivered) Icons.Default.DoneAll else Icons.Default.Done
                            
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = tickColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(PanelBlue)
            ) {
                DropdownMenuItem(
                    text = { Text("Edit", color = PrimaryText) },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp)) },
                    onClick = {
                        showMenu = false
                        onEdit()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete", color = Color.Red) },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp)) },
                    onClick = {
                        showMenu = false
                        onDelete()
                    }
                )
            }
        }
    }
}
