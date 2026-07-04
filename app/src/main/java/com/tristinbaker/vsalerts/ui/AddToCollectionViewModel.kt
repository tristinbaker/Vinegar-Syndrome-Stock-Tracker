package com.tristinbaker.vsalerts.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tristinbaker.vsalerts.data.AppDatabase
import com.tristinbaker.vsalerts.data.CollectionItem
import com.tristinbaker.vsalerts.network.ProductDetail
import com.tristinbaker.vsalerts.network.ProductVariant
import com.tristinbaker.vsalerts.network.SearchProduct
import com.tristinbaker.vsalerts.network.VinegarSyndromeApi
import com.tristinbaker.vsalerts.network.normalizeImageUrl
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val DEFAULT_VENDOR_LABEL = "Vinegar Syndrome"

/**
 * Mirrors AddMovieViewModel's search flow but saves into the owned-collection table instead
 * of setting up a price/stock watch — sold-out items are still valid to log as owned, so unlike
 * AddMovieViewModel this doesn't filter variants by stock.
 */
class AddToCollectionViewModel(application: Application) : AndroidViewModel(application) {
    private val api = VinegarSyndromeApi()
    private val dao = AppDatabase.get(application).collectionItemDao()

    val collectedHandles: StateFlow<Set<String>> = dao.observeAll()
        .map { items -> items.map { it.handle }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    var query by mutableStateOf("")
        private set
    var searchResults by mutableStateOf<List<SearchProduct>>(emptyList())
        private set
    var isSearching by mutableStateOf(false)
        private set
    var selectedProduct by mutableStateOf<ProductDetail?>(null)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var isSaving by mutableStateOf(false)
        private set

    fun onQueryChange(newQuery: String) {
        query = newQuery
    }

    fun search(text: String) {
        if (text.isBlank()) {
            searchResults = emptyList()
            return
        }
        viewModelScope.launch {
            isSearching = true
            errorMessage = null
            try {
                searchResults = api.searchProducts(text)
            } catch (t: Throwable) {
                errorMessage = "Search failed: ${t.message}"
            } finally {
                isSearching = false
            }
        }
    }

    fun selectSearchResult(product: SearchProduct) {
        viewModelScope.launch {
            errorMessage = null
            try {
                selectedProduct = api.fetchProduct(product.handle)
            } catch (t: Throwable) {
                errorMessage = "Couldn't load ${product.title}: ${t.message}"
            }
        }
    }

    fun dismissVariantPicker() {
        selectedProduct = null
    }

    fun confirmVariant(product: ProductDetail, variant: ProductVariant, onSaved: () -> Unit) {
        viewModelScope.launch {
            isSaving = true
            try {
                if (dao.findByHandle(product.handle) != null) {
                    errorMessage = "${product.title} is already in your collection"
                    return@launch
                }
                dao.insert(
                    CollectionItem(
                        title = product.title,
                        handle = product.handle,
                        vendorLabel = product.vendor?.takeIf { it.isNotBlank() } ?: DEFAULT_VENDOR_LABEL,
                        thumbnailUrl = normalizeImageUrl(variant.featuredImage?.src ?: product.featuredImage),
                        addedAt = System.currentTimeMillis(),
                    ),
                )
                selectedProduct = null
                onSaved()
            } catch (t: Throwable) {
                errorMessage = "Couldn't save: ${t.message}"
            } finally {
                isSaving = false
            }
        }
    }
}
