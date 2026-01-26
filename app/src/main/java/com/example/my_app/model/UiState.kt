package com.example.my_app.model

data class UiState(
    val resultText: String = "",
    val errorText: String = "",
    val isValid: Boolean = false
)