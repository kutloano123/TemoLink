package com.example.temolink.data.repository

import com.example.temolink.data.model.Notification
import com.example.temolink.data.model.Order
import com.example.temolink.data.model.OrderStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class OrderRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val notificationRepository = NotificationRepository()

    suspend fun createOrder(order: Order): Result<Unit> {
        return try {
            val docRef = firestore.collection("orders").document()
            val finalOrder = order.copy(orderId = docRef.id)
            docRef.set(finalOrder).await()
            
            // Create notification for farmer
            notificationRepository.createNotification(
                Notification(
                    userId = order.farmerId,
                    title = "New Order Received",
                    message = "${order.buyerName} has requested ${order.quantity}x ${order.produceName}",
                    type = "order"
                )
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrdersForBuyer(buyerId: String): List<Order> {
        return try {
            firestore.collection("orders")
                .whereEqualTo("buyerId", buyerId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Order::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getOrdersForFarmer(farmerId: String): List<Order> {
        return try {
            firestore.collection("orders")
                .whereEqualTo("farmerId", farmerId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Order::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Result<Unit> {
        return try {
            val doc = firestore.collection("orders").document(orderId).get().await()
            val order = doc.toObject(Order::class.java)
            
            firestore.collection("orders")
                .document(orderId)
                .update("status", status)
                .await()
            
            if (order != null) {
                // Create notification for buyer
                notificationRepository.createNotification(
                    Notification(
                        userId = order.buyerId,
                        title = "Order Updated",
                        message = "Your order for ${order.produceName} is now ${status.name}",
                        type = "order"
                    )
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
