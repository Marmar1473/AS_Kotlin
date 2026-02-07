package com.example.my_app.model

import androidx.annotation.DrawableRes

data class CatalogItem(
    val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    val isFavorite: Boolean = false,
    @param:DrawableRes val imageRes: Int
)

sealed interface CatalogUiState {
    data object Loading : CatalogUiState
    data class Success(val items: List<CatalogItem>) : CatalogUiState
    data class Error(val message: String) : CatalogUiState
}
