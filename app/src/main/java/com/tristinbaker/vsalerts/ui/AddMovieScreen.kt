package com.tristinbaker.vsalerts.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tristinbaker.vsalerts.network.SearchProduct
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMovieScreen(
    viewModel: AddMovieViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val trackedHandles by viewModel.trackedHandles.collectAsState()

    LaunchedEffect(viewModel.query) {
        delay(400)
        viewModel.search(viewModel.query)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add a movie") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TextField(
                value = viewModel.query,
                onValueChange = viewModel::onQueryChange,
                placeholder = { Text("e.g. Body of Evidence") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(12.dp),
            )

            viewModel.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 12.dp))
            }

            if (viewModel.isSearching) {
                CircularProgressIndicator(modifier = Modifier.padding(12.dp))
            }

            LazyColumn(
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items(viewModel.searchResults, key = { it.handle }) { product ->
                    SearchResultRow(
                        product = product,
                        isTracked = product.handle in trackedHandles,
                        onClick = { viewModel.selectSearchResult(product) },
                    )
                }
            }
        }
    }

    val selected = viewModel.selectedProduct
    if (selected != null) {
        VariantPickerDialog(
            product = selected,
            isSaving = viewModel.isSaving,
            onDismiss = viewModel::dismissVariantPicker,
            onConfirm = { variant -> viewModel.confirmVariant(selected, variant, onSaved) },
        )
    }
}

@Composable
private fun SearchResultRow(product: SearchProduct, isTracked: Boolean, onClick: () -> Unit) {
    androidx.compose.material3.Surface(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        ListItem(
            headlineContent = { Text(product.title) },
            supportingContent = {
                val priceLabel = product.priceMin?.let { min ->
                    if (product.priceMax != null && product.priceMax != min) "$$min - $${product.priceMax}" else "$$min"
                }
                priceLabel?.let { Text(it) }
            },
            leadingContent = {
                AsyncImage(
                    model = product.image,
                    contentDescription = product.title,
                    modifier = Modifier.size(48.dp),
                )
            },
            trailingContent = {
                if (isTracked) {
                    AssistChip(onClick = onClick, label = { Text("Already tracked") })
                }
            },
        )
    }
}
