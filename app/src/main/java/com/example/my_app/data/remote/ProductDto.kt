package com.example.my_app.data.remote

import com.google.gson.annotations.SerializedName

data class ProductDto(
    val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    val image: String,
    val category: String
)