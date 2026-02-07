package com.example.my_app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.compose.rememberNavController
import com.example.my_app.ui.navigation.AppNavGraph
import com.example.my_app.ui.theme.My_appTheme
import com.example.my_app.viewmodel.CatalogViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: CatalogViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val systemDark = isSystemInDarkTheme()

            val darkThemeState = rememberSaveable { mutableStateOf(systemDark) }

            My_appTheme(darkTheme = darkThemeState.value) {
                val navController = rememberNavController()

                AppNavGraph(
                    navController = navController,
                    viewModel = viewModel,
                    isDarkTheme = darkThemeState.value,
                    onThemeToggle = {
                        darkThemeState.value = !darkThemeState.value
                    }
                )
            }
        }
    }
}
