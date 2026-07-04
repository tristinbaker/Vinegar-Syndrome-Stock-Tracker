package com.tristinbaker.vsalerts.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

private const val ROUTE_HOME = "home"
private const val ROUTE_ADD = "add"
private const val ROUTE_ADD_COLLECTION = "add_collection"
private const val ROUTE_SETTINGS = "settings"

@Composable
fun VsAlertsNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = ROUTE_HOME) {
        composable(ROUTE_HOME) {
            MainScreen(
                onAddMovie = { navController.navigate(ROUTE_ADD) },
                onAddToCollectionFlow = { navController.navigate(ROUTE_ADD_COLLECTION) },
                onSettings = { navController.navigate(ROUTE_SETTINGS) },
            )
        }
        composable(ROUTE_ADD) {
            val viewModel: AddMovieViewModel = viewModel()
            AddMovieScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
        composable(ROUTE_ADD_COLLECTION) {
            val viewModel: AddToCollectionViewModel = viewModel()
            AddToCollectionScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
        composable(ROUTE_SETTINGS) {
            val viewModel: SettingsViewModel = viewModel()
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
