package com.tristinbaker.vsalerts.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tristinbaker.vsalerts.data.TrackedMovie
import kotlinx.coroutines.launch

private const val TAB_ALERTS = 0
private const val TAB_COLLECTION = 1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onAddMovie: () -> Unit,
    onAddToCollectionFlow: () -> Unit,
    onSettings: () -> Unit,
) {
    val homeViewModel: HomeViewModel = viewModel()
    val collectionViewModel: CollectionViewModel = viewModel()
    var selectedTab by remember { mutableIntStateOf(TAB_ALERTS) }
    var pendingStopTrackingPrompt by remember { mutableStateOf<TrackedMovie?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("VS Tracker") },
                actions = {
                    if (selectedTab == TAB_ALERTS) {
                        IconButton(onClick = {
                            homeViewModel.refreshNow()
                            scope.launch { snackbarHostState.showSnackbar("Checking now…") }
                        }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Refresh now")
                        }
                    }
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { if (selectedTab == TAB_ALERTS) onAddMovie() else onAddToCollectionFlow() }) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = if (selectedTab == TAB_ALERTS) "Add movie" else "Add to collection",
                )
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == TAB_ALERTS,
                    onClick = { selectedTab = TAB_ALERTS },
                    text = { Text("Alerts") },
                )
                Tab(
                    selected = selectedTab == TAB_COLLECTION,
                    onClick = { selectedTab = TAB_COLLECTION },
                    text = { Text("Collection") },
                )
            }
            if (selectedTab == TAB_ALERTS) {
                AlertsTabContent(
                    viewModel = homeViewModel,
                    onAddToCollection = { movie ->
                        homeViewModel.addToCollection(movie) { message, added ->
                            scope.launch { snackbarHostState.showSnackbar(message) }
                            if (added) pendingStopTrackingPrompt = movie
                        }
                    },
                )
            } else {
                CollectionTabContent(viewModel = collectionViewModel)
            }
        }
    }

    pendingStopTrackingPrompt?.let { movie ->
        AlertDialog(
            onDismissRequest = { pendingStopTrackingPrompt = null },
            title = { Text("Added to collection") },
            text = { Text("Would you like to stop tracking the stock for this item?") },
            confirmButton = {
                TextButton(onClick = {
                    homeViewModel.delete(movie)
                    pendingStopTrackingPrompt = null
                }) { Text("Stop tracking") }
            },
            dismissButton = {
                TextButton(onClick = { pendingStopTrackingPrompt = null }) { Text("Keep tracking") }
            },
        )
    }
}
