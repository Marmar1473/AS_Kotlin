package com.example.my_app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.my_app.model.CatalogUiState
import com.example.my_app.ui.screens.CatalogGridScreen
import com.example.my_app.ui.screens.DetailsScreen
import com.example.my_app.ui.screens.ProfileScreen
import com.example.my_app.viewmodel.CatalogViewModel

object NavRoutes {
    const val TABS = "tabs"
    const val DETAILS = "details/{itemId}"
    const val PROFILE = "profile"

    fun details(itemId: Int) = "details/$itemId"

    const val TAB_CATALOG = "tab_catalog"
    const val TAB_FAVORITES = "tab_favorites"
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
        startDestination = NavRoutes.TABS
    ) {

        composable(NavRoutes.TABS) {
            TabsScaffold(
                rootNavController = navController,
                viewModel = viewModel
            )
        }

        composable(
            route = NavRoutes.DETAILS,
            arguments = listOf(navArgument("itemId") { type = NavType.IntType })
        ) { entry ->
            val itemId = entry.arguments?.getInt("itemId") ?: -1

            val uiState by viewModel.uiState.collectAsState()
            val item = (uiState as? CatalogUiState.Success)
                ?.items
                ?.firstOrNull { it.id == itemId }

            if (item == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Ошибка: товар не найден")
                }
            } else {
                DetailsScreen(
                    item = item,
                    onToggleFavorite = { viewModel.toggleFavorite(item.id) },
                    onNavigateBack = { navController.navigateUp() }
                )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TabsScaffold(
    rootNavController: NavHostController,
    viewModel: CatalogViewModel
) {
    val tabNavController = rememberNavController()

    val tabBackStack by tabNavController.currentBackStackEntryAsState()
    val currentTabRoute = tabBackStack?.destination?.route ?: NavRoutes.TAB_CATALOG

    val title = when (currentTabRoute) {
        NavRoutes.TAB_FAVORITES -> "Избранные"
        else -> "Каталог товаров"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                actions = {
                    IconButton(onClick = { rootNavController.navigate(NavRoutes.PROFILE) }) {
                        Icon(Icons.Default.Person, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentTabRoute == NavRoutes.TAB_CATALOG,
                    onClick = {
                        tabNavController.navigate(NavRoutes.TAB_CATALOG) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(NavRoutes.TAB_CATALOG) { saveState = true }
                        }
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Каталог") }
                )

                NavigationBarItem(
                    selected = currentTabRoute == NavRoutes.TAB_FAVORITES,
                    onClick = {
                        tabNavController.navigate(NavRoutes.TAB_FAVORITES) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(NavRoutes.TAB_CATALOG) { saveState = true }
                        }
                    },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
                    label = { Text("Избранные") }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = tabNavController,
            startDestination = NavRoutes.TAB_CATALOG,
            modifier = Modifier.padding(padding)
        ) {
            composable(NavRoutes.TAB_CATALOG) {
                val uiState by viewModel.uiState.collectAsState()

                when (val state = uiState) {
                    is CatalogUiState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    is CatalogUiState.Success -> {
                        CatalogGridScreen(
                            items = state.items,
                            onItemClick = { id -> rootNavController.navigate(NavRoutes.details(id)) },
                            onToggleFavorite = { id -> viewModel.toggleFavorite(id) }
                        )
                    }

                    is CatalogUiState.Error -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(state.message)
                        }
                    }
                }
            }

            composable(NavRoutes.TAB_FAVORITES) {
                val uiState by viewModel.uiState.collectAsState()

                when (val state = uiState) {
                    is CatalogUiState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    is CatalogUiState.Success -> {
                        CatalogGridScreen(
                            items = state.items.filter { it.isFavorite },
                            onItemClick = { id -> rootNavController.navigate(NavRoutes.details(id)) },
                            onToggleFavorite = { id -> viewModel.toggleFavorite(id) }
                        )
                    }

                    is CatalogUiState.Error -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(state.message)
                        }
                    }
                }
            }
        }
    }
}
