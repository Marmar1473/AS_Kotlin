package com.example.my_app.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.my_app.model.UiState

class MainViewModel : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    fun calculate(amountStr: String, discountStr: String) {
        val amount = amountStr.toDoubleOrNull()
        val discount = discountStr.toDoubleOrNull()

        when {
            amountStr.isBlank() || discountStr.isBlank() -> {
                _state.value = UiState(
                    errorText = "Заполните все поля",
                    isValid = false
                )
            }
            amount == null || discount == null -> {
                _state.value = UiState(
                    errorText = "Введите корректные числа",
                    isValid = false
                )
            }
            amount < 0 || discount < 0 -> {
                _state.value = UiState(
                    errorText = "Значения не могут быть отрицательными",
                    isValid = false
                )
            }
            discount !in 0.0..90.0 -> {
                _state.value = UiState(
                    errorText = "Скидка должна быть от 0% до 90%",
                    isValid = false
                )
            }
            else -> {
                val finalAmount = amount - (amount * discount / 100)
                _state.value = UiState(
                    resultText = "Итоговая сумма: %.2f ₸".format(finalAmount),
                    errorText = "",
                    isValid = true
                )
            }
        }
    }
}