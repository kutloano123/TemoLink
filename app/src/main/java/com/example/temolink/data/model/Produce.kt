package com.example.temolink.data.model

data class Produce(
    val produceId: String = "",
    val farmerId: String = "",
    val farmerName: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val price: Double = 0.0,
    val unit: String = "kg",
    val quantity: Int = 0,
    val location: String = "",
    val imageUrl: String = "",
    val availability: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
