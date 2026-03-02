package com.example.my_app.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("products")
    suspend fun getProducts(): List<ProductDto>

    @POST("products")
    suspend fun createProduct(@Body product: ProductDto): ProductDto
}