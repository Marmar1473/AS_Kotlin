package com.example.my_app.model

import androidx.annotation.DrawableRes

data class CatalogItem(
    val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    val isFavorite: Boolean = false,
    @DrawableRes val imageRes: Int
)