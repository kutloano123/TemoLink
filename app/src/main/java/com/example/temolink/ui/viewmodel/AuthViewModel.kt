package com.example.temolink.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.temolink.data.model.User
import com.example.temolink.data.model.UserRole
import com.example.temolink.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
    object PasswordResetSent : AuthState()
}

class AuthViewModel(private val repository: AuthRepository = AuthRepository()) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        val uid = repository.getCurrentUserId()
        if (uid != null) {
            viewModelScope.launch {
                val profile = repository.getUserProfile(uid)
                _currentUser.value = profile
                if (profile != null) {
                    _authState.value = AuthState.Success(profile)
                }
            }
        }
    }

    fun login(email: String, password: String) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("Please fill in all fields")
            return
        }
        
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.loginUser(trimmedEmail, password)
                .onSuccess { user ->
                    _currentUser.value = user
                    _authState.value = AuthState.Success(user)
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Login failed")
                }
        }
    }

    fun register(user: User, password: String) {
        if (user.email.isBlank() || password.isBlank() || user.fullName.isBlank()) {
            _authState.value = AuthState.Error("Please fill in all fields")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.registerUser(user.copy(email = user.email.trim()), password)
                .onSuccess { registeredUser ->
                    _currentUser.value = registeredUser
                    _authState.value = AuthState.Success(registeredUser)
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Registration failed")
                }
        }
    }

    fun resetPassword(email: String) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty()) {
            _authState.value = AuthState.Error("Please enter your email address")
            return
        }
        
        // Simple regex for email validation
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            _authState.value = AuthState.Error("Please enter a valid email address")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            repository.sendPasswordResetEmail(trimmedEmail)
                .onSuccess {
                    _authState.value = AuthState.PasswordResetSent
                }
                .onFailure { error ->
                    _authState.value = AuthState.Error(error.message ?: "Failed to send reset email")
                }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun logout() {
        repository.logout()
        _currentUser.value = null
        _authState.value = AuthState.Idle
    }
}
