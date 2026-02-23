package com.example.my_app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogDao {

    @Query("SELECT * FROM catalog_items")
    fun getAllItems(): Flow<List<CatalogItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: CatalogItemEntity)

    @Update
    suspend fun updateItem(item: CatalogItemEntity)

    @Delete
    suspend fun deleteItem(item: CatalogItemEntity)

    @Query("DELETE FROM catalog_items WHERE id = :id")
    suspend fun deleteById(id: Int)
}