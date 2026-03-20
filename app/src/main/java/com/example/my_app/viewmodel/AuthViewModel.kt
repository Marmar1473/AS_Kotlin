package com.example.my_app.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data object Success : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun signUp(email: String, pass: String) {
        Log.d("AuthDebug", "Попытка регистрации: email=$email")

        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthUiState.Error("Заполните все поля")
            return
        }

        if (pass.length < 6) {
            Log.e("AuthDebug", "Ошибка: слишком короткий пароль!")
            _authState.value = AuthUiState.Error("Пароль должен быть не менее 6 символов")
            return
        }

        _authState.value = AuthUiState.Loading
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthUiState.Success
                } else {
                    Log.e("AuthDebug", "Ошибка Firebase: ${task.exception?.message}")
                    _authState.value = AuthUiState.Error(task.exception?.message ?: "Ошибка регистрации")
                }
            }
    }

    fun signIn(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authState.value = AuthUiState.Error("Заполните все поля")
            return
        }
        _authState.value = AuthUiState.Loading
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthUiState.Success
                } else {
                    _authState.value = AuthUiState.Error(task.exception?.message ?: "Ошибка входа")
                }
            }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthUiState.Idle
    }

    fun resetState() {
        _authState.value = AuthUiState.Idle
    }
}