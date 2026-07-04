package com.tristinbaker.vsalerts.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tristinbaker.vsalerts.data.AppDatabase
import com.tristinbaker.vsalerts.data.CollectionItem
import com.tristinbaker.vsalerts.util.VENDOR_LABEL_ORDER
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class CollectionEntry {
    data class Header(val label: String) : CollectionEntry()
    data class Row(val item: CollectionItem) : CollectionEntry()
}

class CollectionViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.get(application).collectionItemDao()

    val entries: StateFlow<List<CollectionEntry>> = dao.observeAll()
        .map { items -> groupByLabel(items) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun delete(item: CollectionItem) {
        viewModelScope.launch { dao.delete(item) }
    }

    private fun groupByLabel(items: List<CollectionItem>): List<CollectionEntry> {
        val grouped = items.groupBy { it.vendorLabel }
        val unknownLabelsSorted = grouped.keys.filter { it !in VENDOR_LABEL_ORDER }.sorted()
        val orderedLabels = VENDOR_LABEL_ORDER.filter { grouped.containsKey(it) } + unknownLabelsSorted

        return orderedLabels.flatMap { label ->
            val rows = grouped[label].orEmpty().sortedBy { it.title }
            listOf(CollectionEntry.Header(label)) + rows.map { CollectionEntry.Row(it) }
        }
    }
}
