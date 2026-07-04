package com.tristinbaker.vsalerts.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tristinbaker.vsalerts.data.TrackedMovie
import com.tristinbaker.vsalerts.util.formatCents
import com.tristinbaker.vsalerts.util.formatStockLabel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AlertsTabContent(
    viewModel: HomeViewModel,
    onAddToCollection: (TrackedMovie) -> Unit,
) {
    val movies by viewModel.movies.collectAsState()

    if (movies.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No movies tracked yet. Tap + to add one.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(movies, key = { it.id }) { movie ->
            MovieRow(
                movie = movie,
                onDelete = { viewModel.delete(movie) },
                onAddToCollection = { onAddToCollection(movie) },
            )
        }
    }
}

@Composable
private fun MovieRow(
    movie: TrackedMovie,
    onDelete: () -> Unit,
    onAddToCollection: () -> Unit,
) {
    val isOnSale = movie.lastCompareAtPriceCents != null && movie.lastCompareAtPriceCents > movie.lastPriceCents
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            val url = "https://vinegarsyndrome.com/products/${movie.handle}"
            context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url)))
        },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = movie.thumbnailUrl,
                contentDescription = movie.title,
                modifier = Modifier.size(56.dp),
            )
            Column(
                modifier = Modifier.weight(1f).padding(start = 12.dp),
            ) {
                Text(movie.title, style = MaterialTheme.typography.titleMedium)
                Text(movie.variantTitle, style = MaterialTheme.typography.bodySmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isOnSale) {
                        Text(
                            formatCents(movie.lastCompareAtPriceCents!!),
                            style = MaterialTheme.typography.bodySmall,
                            textDecoration = TextDecoration.LineThrough,
                            modifier = Modifier.padding(end = 6.dp),
                        )
                    }
                    Text(formatCents(movie.lastPriceCents), style = MaterialTheme.typography.bodyMedium)
                }
                Text(
                    "${formatStockLabel(movie.lastInventoryQty)} · checked ${lastCheckedLabel(movie.lastCheckedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            IconButton(onClick = onAddToCollection) {
                Icon(Icons.Filled.BookmarkAdd, contentDescription = "Add to collection")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Remove")
            }
        }
    }
}

private fun lastCheckedLabel(timestampMs: Long): String {
    if (timestampMs == 0L) return "never"
    return SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(timestampMs))
}
