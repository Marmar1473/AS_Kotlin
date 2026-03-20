package com.example.my_app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.my_app.viewmodel.ApiUiState

@Composable
fun ApiScreen(
    apiState: ApiUiState,
    onRetry: () -> Unit,
    onItemClick: (Int) -> Unit,
    onToggleFavorite: (Int) -> Unit
) {
    when (apiState) {
        is ApiUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text("Загрузка товаров из сети...")
                }
            }
        }

        is ApiUiState.Success -> {
            CatalogGridScreen(
                items = apiState.items,
                onItemClick = onItemClick,
                onToggleFavorite = onToggleFavorite
            )
        }

        is ApiUiState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "⚠️ ${apiState.message}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onRetry) {
                        Text("Повторить")
                    }
                }
            }
        }
    }
}