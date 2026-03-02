package com.example.my_app.data.remote

import retrofit2.http.GET

interface ApiService {
    @GET("products")
    suspend fun getProducts(): List<ProductDto>
}