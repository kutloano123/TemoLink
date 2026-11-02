package com.example.temolink.data.repository

import android.util.Log
import com.example.temolink.data.model.Notification
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NotificationRepository {
    private val firestore = FirebaseFirestore.getInstance()

    fun getNotificationsForUser(userId: String): Flow<List<Notification>> = callbackFlow {
        if (userId.isEmpty()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        
        val subscription = firestore.collection("notifications")
            .whereEqualTo("userId", userId)
            // Removed orderBy to avoid index requirement crash
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("NotificationRepository", "Firestore error: ${error.message}")
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.toObjects(Notification::class.java)
                        .sortedByDescending { it.createdAt }
                    trySend(list)
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun markAsRead(notificationId: String) {
        try {
            firestore.collection("notifications").document(notificationId)
                .update("read", true).await()
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error marking as read", e)
        }
    }

    suspend fun createNotification(notification: Notification) {
        try {
            val docRef = firestore.collection("notifications").document()
            val finalNotification = notification.copy(notificationId = docRef.id)
            docRef.set(finalNotification).await()
        } catch (e: Exception) {
            Log.e("NotificationRepository", "Error creating notification", e)
        }
    }
}
