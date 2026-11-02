package com.example.temolink.data.model

data class MessageThread(
    val threadId: String = "",
    val buyerId: String = "",
    val buyerName: String = "",
    val farmerId: String = "",
    val farmerName: String = "",
    val lastMessage: String = "",
    val lastMessageId: String = "",
    val lastMessageSenderId: String = "",
    val lastMessageRead: Boolean = false,
    val lastMessageDelivered: Boolean = false,
    val lastMessageAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)

data class ChatMessage(
    val messageId: String = "",
    val threadId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val text: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val delivered: Boolean = false,
    val read: Boolean = false,
    val edited: Boolean = false
)
