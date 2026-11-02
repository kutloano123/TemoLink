package com.example.temolink.data.model

data class Notification(
    val notificationId: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "info", // order, message, info
    val read: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
