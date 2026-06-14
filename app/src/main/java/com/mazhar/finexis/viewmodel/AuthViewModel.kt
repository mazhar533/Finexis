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

    private val _isEmailVerified = MutableStateFlow(false)
    val isEmailVerified: StateFlow<Boolean> = _isEmailVerified

    private val _displayName = MutableStateFlow<String>("Mazharalihaider4")
    val displayName: StateFlow<String> = _displayName

    private fun extractDisplayName(email: String?): String {
        if (email.isNullOrBlank()) return "Mazharalihaider4"
        val partBeforeAt = email.substringBefore("@")
        return partBeforeAt.replaceFirstChar { 
            if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() 
        }
    }

    init {
        try {
            auth = FirebaseAuth.getInstance()
            val email = auth?.currentUser?.email
            _currentUser.value = email
            _isEmailVerified.value = auth?.currentUser?.isEmailVerified ?: false
            _displayName.value = auth?.currentUser?.displayName ?: extractDisplayName(email)
        } catch (e: Exception) {
            isMockMode = true
            _currentUser.value = null
            _displayName.value = "Mazharalihaider4"
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
                _displayName.value = extractDisplayName(email)
                onSuccess()
            }
        } else {
            auth?.signInWithEmailAndPassword(email, password)
                ?.addOnCompleteListener { task ->
                    _isLoading.value = false
                    if (task.isSuccessful) {
                        val user = auth?.currentUser
                        _currentUser.value = user?.email
                        _isEmailVerified.value = user?.isEmailVerified ?: false
                        _displayName.value = user?.displayName ?: extractDisplayName(user?.email)
                        onSuccess()
                    } else {
                        _errorMessage.value = task.exception?.localizedMessage ?: "Login failed"
                    }
                }
        }
    }

    fun signUp(email: String, password: String, displayName: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank() || displayName.isBlank()) {
            _errorMessage.value = "Name, Email and Password cannot be empty"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        if (isMockMode || auth == null) {
            viewModelScope.launch {
                delay(1000)
                _isLoading.value = false
                _currentUser.value = email
                _displayName.value = displayName
                onSuccess()
            }
        } else {
            auth?.createUserWithEmailAndPassword(email, password)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth?.currentUser
                        val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                            .setDisplayName(displayName)
                            .build()
                        user?.updateProfile(profileUpdates)?.addOnCompleteListener { updateTask ->
                            _isLoading.value = false
                            if (updateTask.isSuccessful) {
                                _displayName.value = displayName
                            } else {
                                _displayName.value = user.displayName ?: displayName
                            }
                            _currentUser.value = user.email
                            _isEmailVerified.value = user.isEmailVerified
                            onSuccess()
                        }
                    } else {
                        _isLoading.value = false
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

    fun checkEmailVerificationStatus() {
        if (isMockMode || auth == null) {
            // Default to false or simulated state for mock mode
            _isEmailVerified.value = false
            return
        }
        val user = auth?.currentUser
        if (user != null) {
            user.reload().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _isEmailVerified.value = user.isEmailVerified
                }
            }
        }
    }

    fun sendEmailVerification(onComplete: (Boolean, String) -> Unit) {
        if (isMockMode || auth == null) {
            viewModelScope.launch {
                delay(1000)
                _isEmailVerified.value = true // Simulate verification success on mock mode for UX
                onComplete(true, "Verification email sent successfully! (Mock Mode)")
            }
        } else {
            val user = auth?.currentUser
            if (user != null) {
                user.sendEmailVerification()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onComplete(true, "Verification email sent to ${user.email}!")
                        } else {
                            onComplete(false, task.exception?.localizedMessage ?: "Failed to send verification email.")
                        }
                    }
            } else {
                onComplete(false, "No logged in user found.")
            }
        }
    }

    fun updateDisplayName(newName: String, onComplete: (Boolean, String) -> Unit) {
        if (newName.isBlank()) {
            onComplete(false, "Name cannot be empty.")
            return
        }
        _displayName.value = newName
        if (isMockMode || auth == null) {
            viewModelScope.launch {
                delay(500)
                onComplete(true, "Name updated successfully!")
            }
        } else {
            val user = auth?.currentUser
            if (user != null) {
                val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build()
                user.updateProfile(profileUpdates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onComplete(true, "Name updated successfully!")
                        } else {
                            onComplete(false, task.exception?.localizedMessage ?: "Failed to update name.")
                        }
                    }
            } else {
                onComplete(false, "No logged in user found.")
            }
        }
    }

    fun sendPasswordResetEmail(email: String, onComplete: (Boolean, String) -> Unit) {
        if (email.isBlank()) {
            onComplete(false, "Please enter your email address first.")
            return
        }
        _isLoading.value = true
        if (isMockMode || auth == null) {
            viewModelScope.launch {
                delay(1000)
                _isLoading.value = false
                onComplete(true, "Password reset link sent to $email successfully! (Mock Mode)")
            }
        } else {
            auth?.sendPasswordResetEmail(email)
                ?.addOnCompleteListener { task ->
                    _isLoading.value = false
                    if (task.isSuccessful) {
                        onComplete(true, "Password reset link sent to $email successfully!")
                    } else {
                        onComplete(false, task.exception?.localizedMessage ?: "Failed to send reset email.")
                    }
                }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}

