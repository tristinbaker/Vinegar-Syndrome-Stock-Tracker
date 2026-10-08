package com.tristinbaker.vsalerts.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tristinbaker.vsalerts.data.AppDatabase
import com.tristinbaker.vsalerts.data.CollectionItem
import com.tristinbaker.vsalerts.data.TrackedMovie
import com.tristinbaker.vsalerts.worker.runStockCheckNow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.get(application).trackedMovieDao()
    private val collectionDao = AppDatabase.get(application).collectionItemDao()

    val movies: StateFlow<List<TrackedMovie>> = dao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun delete(movie: TrackedMovie) {
        viewModelScope.launch { dao.delete(movie) }
    }

    fun refreshNow() {
        runStockCheckNow(getApplication())
    }

    fun addToCollection(movie: TrackedMovie, onResult: (message: String, added: Boolean) -> Unit) {
        viewModelScope.launch {
            if (collectionDao.find(movie.store, movie.handle) != null) {
                onResult("${movie.title} is already in your collection", false)
                return@launch
            }
            collectionDao.insert(
                CollectionItem(
                    title = movie.title,
                    handle = movie.handle,
                    store = movie.store,
                    vendorLabel = movie.vendorLabel ?: movie.store.displayName,
                    thumbnailUrl = movie.thumbnailUrl,
                    addedAt = System.currentTimeMillis(),
                ),
            )
            onResult("Added ${movie.title} to your collection", true)
        }
    }
}
