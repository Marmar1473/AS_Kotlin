package com.example.my_app.data

import kotlinx.coroutines.flow.Flow

class CatalogRepository(private val dao: CatalogDao) {

    fun getAllItems(): Flow<List<CatalogItemEntity>> = dao.getAllItems()

    suspend fun insert(item: CatalogItemEntity) = dao.insertItem(item)

    suspend fun update(item: CatalogItemEntity) = dao.updateItem(item)

    suspend fun deleteById(id: Int) = dao.deleteById(id)
}