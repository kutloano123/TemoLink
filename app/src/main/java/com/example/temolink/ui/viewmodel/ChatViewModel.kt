package com.example.temolink.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temolink.data.model.ChatMessage
import com.example.temolink.data.model.MessageThread
import com.example.temolink.data.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class ChatViewModel(private val repository: ChatRepository = ChatRepository()) : ViewModel() {

    private val _threads = MutableStateFlow<List<MessageThread>>(emptyList())
    val threads: StateFlow<List<MessageThread>> = _threads.asStateFlow()

    private val _repoMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    private val _pendingMessages = MutableStateFlow<List<ChatMessage>>(emptyList())

    val messages: StateFlow<List<ChatMessage>> = combine(_repoMessages, _pendingMessages) { repo, pending ->
        val repoIds = repo.map { it.messageId }.toSet()
        val actualPending = pending.filter { it.messageId !in repoIds }
        (repo + actualPending).sortedBy { it.createdAt }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    private var messagesJob: Job? = null
    private var threadCollectionJob: Job? = null
    private var currentThreadId: String? = null

    fun getUnreadCount(currentUserId: String): Flow<Int> = _threads.map { list ->
        list.count { it.lastMessageSenderId != currentUserId && !it.lastMessageRead && it.lastMessage.isNotEmpty() }
    }

    fun loadThreads(userId: String, role: String) {
        threadCollectionJob?.cancel()
        threadCollectionJob = viewModelScope.launch {
            repository.getThreadsForUser(userId, role)
                .catch { e -> 
                    Log.e("ChatViewModel", "Error collecting threads", e)
                }
                .collect {
                    _threads.value = it
                }
        }
    }

    fun loadMessages(threadId: String) {
        if (currentThreadId == threadId) return
        currentThreadId = threadId
        
        _repoMessages.value = emptyList()
        _pendingMessages.value = emptyList()
        
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            repository.getMessages(threadId)
                .catch { e -> 
                    Log.e("ChatViewModel", "Error collecting messages", e)
                }
                .collect { msgs ->
                    _repoMessages.value = msgs
                }
        }
    }

    fun sendMessage(threadId: String, senderId: String, receiverId: String, text: String) {
        if (text.isBlank()) return
        
        val optimisticMessage = ChatMessage(
            messageId = UUID.randomUUID().toString(),
            threadId = threadId,
            senderId = senderId,
            receiverId = receiverId,
            text = text,
            createdAt = System.currentTimeMillis()
        )
        
        _pendingMessages.update { it + optimisticMessage }
        
        viewModelScope.launch {
            try {
                repository.sendMessage(optimisticMessage)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending message", e)
                _pendingMessages.update { list -> list.filter { it.messageId != optimisticMessage.messageId } }
            }
        }
    }

    fun deleteMessage(messageId: String) {
        _pendingMessages.update { list -> list.filter { it.messageId != messageId } }
        _repoMessages.update { list -> list.filter { it.messageId != messageId } }
        
        viewModelScope.launch {
            try {
                repository.deleteMessage(messageId)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error deleting message", e)
            }
        }
    }

    fun editMessage(messageId: String, newText: String) {
        if (newText.isBlank()) return
        
        val editMapper: (ChatMessage) -> ChatMessage = { 
            if (it.messageId == messageId) it.copy(text = newText, edited = true) else it 
        }
        
        _pendingMessages.update { it.map(editMapper) }
        _repoMessages.update { it.map(editMapper) }

        viewModelScope.launch {
            try {
                repository.updateMessage(messageId, newText)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error editing message", e)
            }
        }
    }

    fun markAsRead(messageId: String) {
        viewModelScope.launch {
            try {
                repository.markAsRead(messageId)
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error marking as read", e)
            }
        }
    }

    suspend fun getOrCreateThread(
        buyerId: String,
        buyerName: String,
        farmerId: String,
        farmerName: String
    ): String {
        return try {
            repository.getOrCreateThread(buyerId, buyerName, farmerId, farmerName)
        } catch (e: Exception) {
            Log.e("ChatViewModel", "Error getting/creating thread", e)
            ""
        }
    }
}
