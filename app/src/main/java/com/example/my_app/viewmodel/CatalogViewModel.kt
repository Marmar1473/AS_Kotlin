package com.example.my_app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_app.data.AppDatabase
import com.example.my_app.data.CatalogItemEntity
import com.example.my_app.data.CatalogRepository
import com.example.my_app.data.remote.RetrofitProvider
import com.example.my_app.model.CatalogItem
import com.example.my_app.model.CatalogUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
class CatalogViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CatalogRepository

    private val _uiState = MutableStateFlow<CatalogUiState>(CatalogUiState.Loading)
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    private val _apiState = MutableStateFlow<ApiUiState>(ApiUiState.Loading)
    val apiState: StateFlow<ApiUiState> = _apiState.asStateFlow()

    init {
        val dao = AppDatabase.getDatabase(application).catalogDao()
        repository = CatalogRepository(dao, RetrofitProvider.apiService)

        viewModelScope.launch {
            repository.getAllItems()
                .map { entities -> entities.map { it.toCatalogItem() } }
                .collect { items ->
                    _uiState.value = CatalogUiState.Success(items)
                }
        }

        loadFromApi()
    }

    fun loadFromApi() {
        viewModelScope.launch {
            _apiState.value = ApiUiState.Loading
            val result = repository.fetchProductsFromApi()
            result.fold(
                onSuccess = { items -> _apiState.value = ApiUiState.Success(items) },
                onFailure = { e -> _apiState.value = ApiUiState.Error(e.message ?: "Неизвестная ошибка") }
            )
        }
    }

    fun addItem(title: String, description: String, price: Double, imageUri: String? = null) {
        viewModelScope.launch {
            repository.insert(
                CatalogItemEntity(
                    title = title,
                    description = description,
                    price = price,
                    imageUri = imageUri
                )
            )
        }
    }

    fun updateItem(id: Int, title: String, description: String, price: Double, imageUri: String? = null) {
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
                    imageUri = imageUri ?: current.imageUri
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
            // Ищем в локальной БД
            val fromDb = (uiState.value as? CatalogUiState.Success)
                ?.items?.firstOrNull { it.id == id }

            if (fromDb != null) {
                // Товар уже в БД — просто инвертируем isFavorite
                repository.update(
                    CatalogItemEntity(
                        id = fromDb.id,
                        title = fromDb.title,
                        description = fromDb.description,
                        price = fromDb.price,
                        isFavorite = !fromDb.isFavorite,
                        imageUri = fromDb.imageUri
                    )
                )
            } else {
                // Товара нет в БД — ищем в API и сохраняем с isFavorite = true
                val fromApi = (apiState.value as? ApiUiState.Success)
                    ?.items?.firstOrNull { it.id == id } ?: return@launch

                repository.insert(
                    CatalogItemEntity(
                        id = fromApi.id,
                        title = fromApi.title,
                        description = fromApi.description,
                        price = fromApi.price,
                        isFavorite = true,
                        imageUri = fromApi.imageUri
                    )
                )
            }
        }
    }
}

sealed interface ApiUiState {
    data object Loading : ApiUiState
    data class Success(val items: List<CatalogItem>) : ApiUiState
    data class Error(val message: String) : ApiUiState
}

fun CatalogItemEntity.toCatalogItem() = CatalogItem(
    id = id,
    title = title,
    description = description,
    price = price,
    isFavorite = isFavorite,
    imageUri = imageUri
)