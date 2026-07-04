package com.tristinbaker.vsalerts.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tristinbaker.vsalerts.data.AppDatabase
import com.tristinbaker.vsalerts.data.TrackedMovie
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

class AddMovieViewModel(application: Application) : AndroidViewModel(application) {
    private val api = VinegarSyndromeApi()
    private val dao = AppDatabase.get(application).trackedMovieDao()

    val trackedHandles: StateFlow<Set<String>> = dao.observeAll()
        .map { movies -> movies.map { it.handle }.toSet() }
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
                val detail = api.fetchProduct(product.handle)
                val inStockVariants = detail.variants.filter { it.isInStock() }
                if (inStockVariants.isEmpty()) {
                    errorMessage = "${product.title} is sold out"
                    return@launch
                }
                selectedProduct = detail.copy(variants = inStockVariants)
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
                    errorMessage = "${product.title} is already being tracked"
                    return@launch
                }
                dao.insert(
                    TrackedMovie(
                        title = product.title,
                        handle = product.handle,
                        variantId = variant.id,
                        variantTitle = variant.title,
                        thumbnailUrl = normalizeImageUrl(variant.featuredImage?.src ?: product.featuredImage),
                        lastPriceCents = variant.price,
                        lastCompareAtPriceCents = variant.compareAtPrice,
                        lastInventoryQty = variant.inventoryQuantity ?: 0,
                        lastCheckedAt = System.currentTimeMillis(),
                        vendorLabel = product.vendor,
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

/** Sold-out variants (available == false, or explicitly 0 units) aren't worth tracking. */
private fun ProductVariant.isInStock(): Boolean =
    available && (inventoryQuantity == null || inventoryQuantity > 0)
