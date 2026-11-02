package com.example.temolink.data.repository

import android.util.Log
import com.example.temolink.data.model.ChatMessage
import com.example.temolink.data.model.MessageThread
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getOrCreateThread(
        buyerId: String,
        buyerName: String,
        farmerId: String,
        farmerName: String
    ): String {
        val existing = firestore.collection("messageThreads")
            .whereEqualTo("buyerId", buyerId)
            .whereEqualTo("farmerId", farmerId)
            .get()
            .await()

        if (!existing.isEmpty) {
            return existing.documents[0].id
        }

        val docRef = firestore.collection("messageThreads").document()
        val thread = MessageThread(
            threadId = docRef.id,
            buyerId = buyerId,
            buyerName = buyerName,
            farmerId = farmerId,
            farmerName = farmerName,
            lastMessageAt = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis()
        )
        docRef.set(thread).await()
        return docRef.id
    }

    fun getThreadsForUser(userId: String, role: String): Flow<List<MessageThread>> = callbackFlow {
        if (userId.isEmpty()) {
            trySend(emptyList())
            return@callbackFlow
        }
        val field = if (role == "FARMER") "farmerId" else "buyerId"
        
        val subscription = firestore.collection("messageThreads")
            .whereEqualTo(field, userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatRepository", "Error listening for threads: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val threads = snapshot.toObjects(MessageThread::class.java)
                        .sortedByDescending { it.lastMessageAt }
                    trySend(threads)
                }
            }
        awaitClose { subscription.remove() }
    }

    fun getMessages(threadId: String): Flow<List<ChatMessage>> = callbackFlow {
        if (threadId.isEmpty()) {
            trySend(emptyList())
            return@callbackFlow
        }
        
        val subscription = firestore.collection("messages")
            .whereEqualTo("threadId", threadId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ChatRepository", "Error listening for messages: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val msgs = snapshot.toObjects(ChatMessage::class.java)
                        .sortedBy { it.createdAt }
                    trySend(msgs)
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun sendMessage(message: ChatMessage): Result<Unit> {
        return try {
            val docRef = if (message.messageId.isNotEmpty()) {
                firestore.collection("messages").document(message.messageId)
            } else {
                firestore.collection("messages").document()
            }
            
            val finalMsg = if (message.messageId.isEmpty()) {
                message.copy(messageId = docRef.id)
            } else {
                message
            }
            
            docRef.set(finalMsg).await()

            // Update thread metadata including unread status
            firestore.collection("messageThreads").document(message.threadId)
                .update(
                    "lastMessage", message.text,
                    "lastMessageId", finalMsg.messageId,
                    "lastMessageSenderId", message.senderId,
                    "lastMessageRead", false,
                    "lastMessageAt", message.createdAt
                ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error sending message", e)
            Result.failure(e)
        }
    }

    suspend fun deleteMessage(messageId: String): Result<Unit> {
        return try {
            if (messageId.isEmpty()) return Result.failure(Exception("Message ID is empty"))
            
            val messageDoc = firestore.collection("messages").document(messageId).get().await()
            val threadId = messageDoc.getString("threadId") ?: ""
            
            firestore.collection("messages").document(messageId).delete().await()
            
            // If this was the last message, update the thread's last message info
            if (threadId.isNotEmpty()) {
                // Fetch messages for this thread to find the new latest one
                // We fetch all and sort manually to avoid index requirement during development
                val messagesSnapshot = firestore.collection("messages")
                    .whereEqualTo("threadId", threadId)
                    .get()
                    .await()
                
                val threadRef = firestore.collection("messageThreads").document(threadId)
                if (messagesSnapshot.isEmpty) {
                    // No messages left in thread
                    threadRef.update(
                        "lastMessage", "",
                        "lastMessageId", "",
                        "lastMessageAt", System.currentTimeMillis(),
                        "lastMessageRead", true
                    ).await()
                } else {
                    val lastMsg = messagesSnapshot.toObjects(ChatMessage::class.java)
                        .sortedByDescending { it.createdAt }
                        .firstOrNull()
                        
                    if (lastMsg != null) {
                        threadRef.update(
                            "lastMessage", lastMsg.text,
                            "lastMessageId", lastMsg.messageId,
                            "lastMessageSenderId", lastMsg.senderId,
                            "lastMessageRead", lastMsg.read,
                            "lastMessageAt", lastMsg.createdAt
                        ).await()
                    }
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error deleting message: $messageId", e)
            Result.failure(e)
        }
    }

    suspend fun updateMessage(messageId: String, newText: String): Result<Unit> {
        return try {
            firestore.collection("messages").document(messageId)
                .update("text", newText, "edited", true)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAsDelivered(messageId: String) {
        try {
            firestore.collection("messages").document(messageId)
                .update("delivered", true)
                .await()
        } catch (e: Exception) {
            // Silently fail or log
        }
    }

    suspend fun markAsRead(messageId: String) {
        try {
            val docRef = firestore.collection("messages").document(messageId)
            val snapshot = docRef.get().await()
            val threadId = snapshot.getString("threadId")
            val msgCreatedAt = snapshot.getLong("createdAt") ?: 0L
            
            docRef.update("read", true, "delivered", true).await()
            
            // Also update thread if this is the latest message
            if (threadId != null) {
                val threadRef = firestore.collection("messageThreads").document(threadId)
                val threadDoc = threadRef.get().await()
                val lastMsgAt = threadDoc.getLong("lastMessageAt") ?: 0L
                
                // Only mark as read in thread if this is the message the thread is tracking
                if (msgCreatedAt >= lastMsgAt) {
                    threadRef.update("lastMessageRead", true).await()
                }
            }
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error marking message as read", e)
        }
    }
}
