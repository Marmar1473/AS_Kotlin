package com.example.my_app.data

import com.example.my_app.data.remote.ApiService
import com.example.my_app.data.remote.ProductDto
import com.example.my_app.model.CatalogItem
import kotlinx.coroutines.flow.Flow
import java.io.IOException

class CatalogRepository(
    private val dao: CatalogDao,
    private val apiService: ApiService
) {
    fun getAllItems(): Flow<List<CatalogItemEntity>> = dao.getAllItems()

    suspend fun insert(item: CatalogItemEntity) = dao.insertItem(item)

    suspend fun update(item: CatalogItemEntity) = dao.updateItem(item)

    suspend fun deleteById(id: Int) = dao.deleteById(id)

    suspend fun fetchProductsFromApi(): Result<List<CatalogItem>> {
        return try {
            val products = apiService.getProducts()
            val items = products.map { dto ->
                CatalogItem(
                    id = dto.id,
                    title = dto.title,
                    description = dto.description,
                    price = dto.price,
                    imageUri = dto.image
                )
            }
            Result.success(items)
        } catch (e: IOException) {
            Result.failure(Exception("Нет подключения к интернету"))
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка загрузки: ${e.message}"))
        }
    }

    suspend fun createProductOnApi(item: CatalogItem): Result<CatalogItem> {
        return try {
            val dto = ProductDto(
                id = 0,
                title = item.title,
                description = item.description,
                price = item.price,
                image = item.imageUri ?: "",
                category = "general"
            )
            val result = apiService.createProduct(dto)
            Result.success(
                CatalogItem(
                    id = result.id,
                    title = result.title,
                    description = result.description,
                    price = result.price,
                    imageUri = result.image
                )
            )
        } catch (e: IOException) {
            Result.failure(Exception("Нет подключения к интернету"))
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка: ${e.message}"))
        }
    }
}