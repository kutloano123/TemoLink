package com.example.temolink.data.model

enum class OrderStatus {
    PENDING, CONFIRMED, COMPLETED, CANCELLED
}

data class Order(
    val orderId: String = "",
    val buyerId: String = "",
    val buyerName: String = "",
    val farmerId: String = "",
    val produceId: String = "",
    val produceName: String = "",
    val quantity: Int = 1,
    val price: Double = 0.0,
    val totalPrice: Double = 0.0,
    val status: OrderStatus = OrderStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis()
)
