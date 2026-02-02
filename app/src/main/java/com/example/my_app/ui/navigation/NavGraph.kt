package com.example.my_app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.my_app.model.CatalogUiState
import com.example.my_app.ui.screens.CatalogScreen
import com.example.my_app.ui.screens.DetailsScreen
import com.example.my_app.ui.screens.ProfileScreen
import com.example.my_app.viewmodel.CatalogViewModel

object NavRoutes {
    const val CATALOG = "catalog"
    const val DETAILS = "details/{itemId}"
    const val PROFILE = "profile"

    fun details(itemId: Int) = "details/$itemId"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    viewModel: CatalogViewModel,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.CATALOG
    ) {
        composable(NavRoutes.CATALOG) {
            val uiState by viewModel.uiState.collectAsState()

            when (val state = uiState) {
                is CatalogUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is CatalogUiState.Success -> {
                    CatalogScreen(
                        items = state.items,
                        onItemClick = { itemId ->
                            navController.navigate(NavRoutes.details(itemId))
                        },
                        onProfileClick = {
                            navController.navigate(NavRoutes.PROFILE)
                        }
                    )
                }
                is CatalogUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message)
                    }
                }
            }
        }

        composable(
            route = NavRoutes.DETAILS,
            arguments = listOf(navArgument("itemId") { type = NavType.IntType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("itemId")
            val item = itemId?.let { viewModel.getItemById(it) }

            if (item != null) {
                DetailsScreen(
                    item = item,
                    onToggleFavorite = { viewModel.toggleFavorite(item.id) },
                    onNavigateBack = { navController.navigateUp() }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: Item not found")
                }
            }
        }

        composable(NavRoutes.PROFILE) {
            ProfileScreen(
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle,
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}