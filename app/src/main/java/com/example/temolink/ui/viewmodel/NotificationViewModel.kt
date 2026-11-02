package com.example.temolink.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temolink.data.model.Notification
import com.example.temolink.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class NotificationViewModel(private val repository: NotificationRepository = NotificationRepository()) : ViewModel() {

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    fun loadNotifications(userId: String) {
        viewModelScope.launch {
            repository.getNotificationsForUser(userId)
                .catch { e ->
                    Log.e("NotificationViewModel", "Error loading notifications", e)
                }
                .collect { list ->
                    _notifications.value = list
                    _unreadCount.value = list.count { !it.read }
                }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            repository.markAsRead(notificationId)
        }
    }
}
