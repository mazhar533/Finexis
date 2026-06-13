package com.mazhar.finexis.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private var auth: FirebaseAuth? = null
    
    private val _currentUser = MutableStateFlow<String?>(null)
    val currentUser: StateFlow<String?> = _currentUser

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var isMockMode = false

    init {
        try {
            auth = FirebaseAuth.getInstance()
            _currentUser.value = auth?.currentUser?.email
        } catch (e: Exception) {
            isMockMode = true
            _currentUser.value = null
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Email and Password cannot be empty"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        if (isMockMode || auth == null) {
            viewModelScope.launch {
                delay(1000)
                _isLoading.value = false
                _currentUser.value = email
                onSuccess()
            }
        } else {
            auth?.signInWithEmailAndPassword(email, password)
                ?.addOnCompleteListener { task ->
                    _isLoading.value = false
                    if (task.isSuccessful) {
                        _currentUser.value = auth?.currentUser?.email
                        onSuccess()
                    } else {
                        _errorMessage.value = task.exception?.localizedMessage ?: "Login failed"
                    }
                }
        }
    }

    fun signUp(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Email and Password cannot be empty"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        if (isMockMode || auth == null) {
            viewModelScope.launch {
                delay(1000)
                _isLoading.value = false
                _currentUser.value = email
                onSuccess()
            }
        } else {
            auth?.createUserWithEmailAndPassword(email, password)
                ?.addOnCompleteListener { task ->
                    _isLoading.value = false
                    if (task.isSuccessful) {
                        _currentUser.value = auth?.currentUser?.email
                        onSuccess()
                    } else {
                        _errorMessage.value = task.exception?.localizedMessage ?: "Signup failed"
                    }
                }
        }
    }

    fun logout() {
        if (isMockMode || auth == null) {
            _currentUser.value = null
        } else {
            auth?.signOut()
            _currentUser.value = null
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
