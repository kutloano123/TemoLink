package com.example.temolink.data.model

import com.google.firebase.firestore.PropertyName

enum class UserRole {
    FARMER, BUYER
}

data class User(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val role: UserRole = UserRole.BUYER,
    val location: String = "",
    val profileImageUrl: String = "",
    val about: String = "",
    // Farmer specific fields
    val farmName: String = "",
    val farmType: String = "",
    val produceTypes: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
