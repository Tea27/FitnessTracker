package com.tbasic.fitnesstracker.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.tbasic.fitnesstracker.data.AppUser
import com.tbasic.fitnesstracker.data.UserPreferences
import com.tbasic.fitnesstracker.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val userId: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    val savedEmail: Flow<String?> = userPreferences.emailFlow

    val isUserLoggedIn = authState.map { state ->
        state is AuthState.Success
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            _authState.value = AuthState.Success(currentUser.uid)
        }
    }

    fun register(email: String, password: String, firstName: String, lastName: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val user = auth.createUserWithEmailAndPassword(email, password).await().user
                if (user != null) {
                    val appUser = AppUser(
                        id = user.uid,
                        email = email,
                        firstName = firstName,
                        lastName = lastName
                    )
                    userRepository.saveUserProfile(appUser)
                    _authState.value = AuthState.Success(user.uid)
                } else {
                    _authState.value = AuthState.Error("Registration failed")
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun login(email: String, password: String) = viewModelScope.launch {
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    viewModelScope.launch {
                        userPreferences.saveEmail(email)
                    }
                    _authState.value = AuthState.Success(auth.currentUser?.uid ?: "")
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Login failed")
                }
            }
    }

    fun logout() {
        auth.signOut()
        _authState.value = AuthState.Idle
    }

    fun changePassword(currentPassword: String, newPassword: String, onResult: (Boolean, String) -> Unit) {
        val user = auth.currentUser
        if (user != null && user.email != null) {
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

            viewModelScope.launch {
                user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                onResult(true, "Password changed successfully.")
                            } else {
                                onResult(false, updateTask.exception?.localizedMessage ?: "Failed to update password.")
                            }
                        }
                    } else {
                        onResult(false, "Reauthentication failed. Check your current password.")
                    }
                }
            }
        } else {
            onResult(false, "User not logged in.")
        }
    }

    fun isUserLoggedIn(): Boolean = auth.currentUser != null
    fun currentUserId(): String? = auth.currentUser?.uid
}
