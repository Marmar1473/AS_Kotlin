package com.example.my_app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.my_app.ui.screens.AddEditItemDialog
import com.example.my_app.ui.screens.ApiScreen
import com.example.my_app.ui.screens.CatalogGridScreen
import com.example.my_app.ui.screens.DetailsScreen
import com.example.my_app.ui.screens.LoginScreen
import com.example.my_app.ui.screens.ProfileScreen
import com.example.my_app.ui.screens.RegisterScreen
import com.example.my_app.viewmodel.ApiUiState
import com.example.my_app.viewmodel.AuthViewModel
import com.example.my_app.viewmodel.CatalogViewModel

object NavRoutes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val TABS = "tabs"
    const val DETAILS = "details/{itemId}"
    const val PROFILE = "profile"

    fun details(itemId: Int) = "details/$itemId"

    const val TAB_CATALOG = "tab_catalog"
    const val TAB_FAVORITES = "tab_favorites"
    const val TAB_API = "tab_api"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    catalogViewModel: CatalogViewModel,
    authViewModel: AuthViewModel,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    val startDest = if (authViewModel.isUserLoggedIn()) NavRoutes.TABS else NavRoutes.LOGIN

    NavHost(
        navController = navController,
        startDestination = startDest
    ) {
        composable(NavRoutes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = {
                    navController.navigate(NavRoutes.REGISTER) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                },
                onLoginSuccess = {
                    navController.navigate(NavRoutes.TABS) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.REGISTER) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = {
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(NavRoutes.REGISTER) { inclusive = true }
                    }
                },
                onRegisterSuccess = {
                    navController.navigate(NavRoutes.TABS) {
                        popUpTo(NavRoutes.REGISTER) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.TABS) {
            TabsScaffold(
                rootNavController = navController,
                viewModel = catalogViewModel
            )
        }

        composable(
            route = NavRoutes.DETAILS,
            arguments = listOf(navArgument("itemId") { type = NavType.IntType })
        ) { entry ->
            val itemId = entry.arguments?.getInt("itemId") ?: -1

            val uiState by catalogViewModel.uiState.collectAsState()
            val apiState by catalogViewModel.apiState.collectAsState()

            val item = (uiState as? CatalogUiState.Success)
                ?.items?.firstOrNull { it.id == itemId }
                ?: (apiState as? ApiUiState.Success)
                    ?.items?.firstOrNull { it.id == itemId }

            if (item == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Ошибка: товар не найден")
                }
            } else {
                DetailsScreen(
                    item = item,
                    onToggleFavorite = { catalogViewModel.toggleFavorite(item.id) },
                    onUpdate = { t, d, p, uri ->
                        catalogViewModel.updateItem(item.id, t, d, p, uri)
                    },
                    onDelete = {
                        catalogViewModel.deleteItem(item.id)
                        navController.navigateUp()
                    },
                    onNavigateBack = { navController.navigateUp() }
                )
            }
        }

        composable(NavRoutes.PROFILE) {
            ProfileScreen(
                isDarkTheme = isDarkTheme,
                onThemeToggle = onThemeToggle,
                onNavigateBack = { navController.navigateUp() },
                onLogout = {
                    authViewModel.signOut()
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(NavRoutes.TABS) { inclusive = true }
                    }
                }
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
        NavRoutes.TAB_API -> "Товары из сети"
        else -> "Каталог товаров"
    }

    var showAddDialog by remember { mutableStateOf(false) }

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

                NavigationBarItem(
                    selected = currentTabRoute == NavRoutes.TAB_API,
                    onClick = {
                        tabNavController.navigate(NavRoutes.TAB_API) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(NavRoutes.TAB_CATALOG) { saveState = true }
                        }
                    },
                    icon = { Icon(Icons.Default.Cloud, contentDescription = null) },
                    label = { Text("Из сети") }
                )
            }
        },
        floatingActionButton = {
            if (currentTabRoute == NavRoutes.TAB_CATALOG) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить товар")
                }
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

            composable(NavRoutes.TAB_API) {
                val apiState by viewModel.apiState.collectAsState()
                ApiScreen(
                    apiState = apiState,
                    onRetry = { viewModel.loadFromApi() },
                    onItemClick = { id -> rootNavController.navigate(NavRoutes.details(id)) },
                    onToggleFavorite = { id -> viewModel.toggleFavorite(id) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddEditItemDialog(
            title = "Добавить товар",
            onDismiss = {
                showAddDialog = false
            },
            onConfirm = { t, d, p, uri ->
                viewModel.addItem(t, d, p, uri)
                showAddDialog = false
            }
        )
    }
}