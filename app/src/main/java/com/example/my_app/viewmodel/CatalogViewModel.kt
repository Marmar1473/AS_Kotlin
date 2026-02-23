package com.example.my_app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_app.data.AppDatabase
import com.example.my_app.data.CatalogItemEntity
import com.example.my_app.data.CatalogRepository
import com.example.my_app.model.CatalogItem
import com.example.my_app.model.CatalogUiState
import com.example.my_app.R
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CatalogViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CatalogRepository

    private val _uiState = MutableStateFlow<CatalogUiState>(CatalogUiState.Loading)
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    init {
        val dao = AppDatabase.getDatabase(application).catalogDao()
        repository = CatalogRepository(dao)

        viewModelScope.launch {
            repository.getAllItems()
                .map { entities ->
                    entities.map { it.toCatalogItem() }
                }
                .collect { items ->
                    _uiState.value = CatalogUiState.Success(items)
                }
        }
    }

    fun addItem(title: String, description: String, price: Double) {
        viewModelScope.launch {
            repository.insert(
                CatalogItemEntity(
                    title = title,
                    description = description,
                    price = price
                )
            )
        }
    }

    fun updateItem(id: Int, title: String, description: String, price: Double) {
        viewModelScope.launch {
            val current = (uiState.value as? CatalogUiState.Success)
                ?.items?.firstOrNull { it.id == id } ?: return@launch
            repository.update(
                CatalogItemEntity(
                    id = id,
                    title = title,
                    description = description,
                    price = price,
                    isFavorite = current.isFavorite,
                )
            )
        }
    }

    fun deleteItem(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun toggleFavorite(id: Int) {
        viewModelScope.launch {
            val current = (uiState.value as? CatalogUiState.Success)
                ?.items?.firstOrNull { it.id == id } ?: return@launch
            repository.update(
                CatalogItemEntity(
                    id = id,
                    title = current.title,
                    description = current.description,
                    price = current.price,
                    isFavorite = !current.isFavorite,
                )
            )
        }
    }
}

fun CatalogItemEntity.toCatalogItem() = CatalogItem(
    id = id,
    title = title,
    description = description,
    price = price,
    isFavorite = isFavorite,
    imageRes = R.drawable.ic_launcher_background
)