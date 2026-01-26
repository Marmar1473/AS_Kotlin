package com.example.my_app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.my_app.ui.screens.CatalogScreen
import com.example.my_app.ui.screens.DetailsScreen
import com.example.my_app.viewmodel.CatalogViewModel

object NavRoutes {
    const val CATALOG = "catalog"
    const val DETAILS = "details/{itemId}"

    fun details(itemId: Int) = "details/$itemId"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    viewModel: CatalogViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.CATALOG
    ) {
        composable(NavRoutes.CATALOG) {
            val items by viewModel.items.collectAsState()
            CatalogScreen(
                items = items,
                onItemClick = { itemId ->
                    navController.navigate(NavRoutes.details(itemId))
                }
            )
        }

        composable(
            route = NavRoutes.DETAILS,
            arguments = listOf(
                navArgument("itemId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("itemId") ?: return@composable
            val item = viewModel.getItemById(itemId)

            if (item != null) {
                DetailsScreen(
                    item = item,
                    onToggleFavorite = { viewModel.toggleFavorite(itemId) },
                    onNavigateBack = { navController.navigateUp() }
                )
            }
        }
    }
}