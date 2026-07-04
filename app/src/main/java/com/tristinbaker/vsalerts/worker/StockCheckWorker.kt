package com.tristinbaker.vsalerts.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tristinbaker.vsalerts.data.AppDatabase
import com.tristinbaker.vsalerts.data.AppSettings
import com.tristinbaker.vsalerts.data.TrackedMovie
import com.tristinbaker.vsalerts.network.VinegarSyndromeApi
import com.tristinbaker.vsalerts.network.normalizeImageUrl
import com.tristinbaker.vsalerts.notification.postAlertNotification
import com.tristinbaker.vsalerts.util.formatCents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Re-checks every tracked movie's price/stock and fires alerts on meaningful changes. */
class StockCheckWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    private val api = VinegarSyndromeApi()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val dao = AppDatabase.get(applicationContext).trackedMovieDao()
        val settings = AppSettings.get(applicationContext)
        val movies = dao.getAll()

        for (movie in movies) {
            try {
                checkMovie(movie, dao, settings)
            } catch (t: Throwable) {
                // One bad fetch shouldn't sink the whole batch; move on to the next movie.
                continue
            }
        }

        Result.success()
    }

    private suspend fun checkMovie(movie: TrackedMovie, dao: com.tristinbaker.vsalerts.data.TrackedMovieDao, settings: AppSettings) {
        val product = api.fetchProduct(movie.handle)
        val variant = product.variants.firstOrNull { it.id == movie.variantId } ?: return

        val wasOnSale = movie.lastCompareAtPriceCents != null && movie.lastCompareAtPriceCents > movie.lastPriceCents
        val isOnSale = variant.compareAtPrice != null && variant.compareAtPrice > variant.price
        val newQty = variant.inventoryQuantity ?: movie.lastInventoryQty

        val messages = mutableListOf<String>()

        if (settings.alertOnPriceDrop && variant.price < movie.lastPriceCents) {
            messages += "price dropped to ${formatCents(variant.price)} (was ${formatCents(movie.lastPriceCents)})"
        }
        if (settings.alertOnSale && isOnSale && !wasOnSale) {
            messages += "on sale now: ${formatCents(variant.price)}"
        }
        if (newQty < settings.stockThreshold && movie.lastInventoryQty >= settings.stockThreshold) {
            messages += "only $newQty left in stock"
        }

        if (messages.isNotEmpty()) {
            postAlertNotification(
                context = applicationContext,
                notificationId = movie.id.toInt(),
                title = movie.title,
                message = messages.joinToString(" · ").replaceFirstChar { it.uppercase() },
                productUrl = "https://vinegarsyndrome.com/products/${movie.handle}",
            )
        }

        val thumbnailUrl = movie.thumbnailUrl
            ?: normalizeImageUrl(variant.featuredImage?.src ?: product.featuredImage)
        val vendorLabel = movie.vendorLabel ?: product.vendor

        dao.update(
            movie.copy(
                lastPriceCents = variant.price,
                lastCompareAtPriceCents = variant.compareAtPrice,
                lastInventoryQty = newQty,
                lastCheckedAt = System.currentTimeMillis(),
                thumbnailUrl = thumbnailUrl,
                vendorLabel = vendorLabel,
            ),
        )
    }
}
