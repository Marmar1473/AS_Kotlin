package com.example.my_app.data

import com.example.my_app.data.remote.ApiService
import com.example.my_app.model.CatalogItem
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.io.IOException

class CatalogRepository(
    private val dao: CatalogDao,
    private val apiService: ApiService
) {
    private val firestore = FirebaseFirestore.getInstance()
    private val collection = firestore.collection("catalog_items")

    fun getItemsFromFirestore(): Flow<List<CatalogItem>> = callbackFlow {
        val listener = collection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val items = snapshot.documents.mapNotNull { doc ->
                    CatalogItem(
                        id = doc.getLong("id")?.toInt() ?: doc.id.hashCode(),
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        price = doc.getDouble("price") ?: 0.0,
                        isFavorite = doc.getBoolean("isFavorite") ?: false,
                        imageUri = doc.getString("imageUri")
                    )
                }
                trySend(items)
            }
        }
        awaitClose { listener.remove() }
    }

    suspend fun saveItemToFirestore(item: CatalogItem): Result<Unit> {
        return try {
            val data = hashMapOf(
                "id" to if (item.id == 0) System.currentTimeMillis().toInt() else item.id,
                "title" to item.title,
                "description" to item.description,
                "price" to item.price,
                "isFavorite" to item.isFavorite,
                "imageUri" to item.imageUri
            )
            collection.document(data["id"].toString()).set(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateInFirestore(item: CatalogItem): Result<Unit> {
        return try {
            val data = mapOf(
                "title" to item.title,
                "description" to item.description,
                "price" to item.price,
                "isFavorite" to item.isFavorite,
                "imageUri" to item.imageUri
            )
            collection.document(item.id.toString()).update(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteFromFirestore(id: Int): Result<Unit> {
        return try {
            collection.document(id.toString()).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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

}