package com.tristinbaker.vsalerts.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.io.IOException

private const val BASE_URL = "https://vinegarsyndrome.com"
private const val USER_AGENT =
    "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Mobile Safari/537.36"

class ProductNotFoundException(handle: String) : IOException("No inventory data found for handle: $handle")

/**
 * Talks to vinegarsyndrome.com's public Shopify storefront endpoints.
 * No API key: search uses the storefront predictive-search endpoint, product detail comes from
 * the `/products/{handle}.js` endpoint, and inventory is scraped from the product page.
 */
class VinegarSyndromeApi(
    private val client: OkHttpClient = OkHttpClient(),
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun searchProducts(query: String): List<SearchProduct> = withContext(Dispatchers.IO) {
        val url = "$BASE_URL/search/suggest.json".toHttpUrl().newBuilder()
            .addQueryParameter("q", query)
            .addQueryParameter("resources[type]", "product")
            .addQueryParameter("resources[limit]", "8")
            .build()

        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .header("Accept", "application/json")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Search failed: HTTP ${response.code}")
            val body = response.body?.string().orEmpty()
            json.decodeFromString<SuggestResponse>(body).resources.results.products
        }
    }

    suspend fun fetchProduct(handle: String): ProductDetail = withContext(Dispatchers.IO) {
        val productJson = get("$BASE_URL/products/$handle.js", accept = "application/json")
        val product = json.decodeFromString<ProductDetail>(productJson)

        val inventory = fetchInventory(handle)
        product.copy(
            variants = product.variants.map { variant ->
                variant.copy(inventoryQuantity = inventory[variant.id.toString()]?.quantity)
            },
        )
    }

    /**
     * Per-variant inventory isn't in the `.js` payload; the theme exposes it as a JSON map on the
     * product page's `<product-inventory data-vs-inventory="{variantId: {q, m, p}}">` element.
     */
    private fun fetchInventory(handle: String): Map<String, VariantInventory> {
        val html = get("$BASE_URL/products/$handle")
        val inventoryJson = Jsoup.parse(html).selectFirst("product-inventory[data-vs-inventory]")
            ?.attr("data-vs-inventory")
            ?: throw ProductNotFoundException(handle)
        return json.decodeFromString(inventoryJson)
    }

    private fun get(url: String, accept: String? = null): String {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .apply { if (accept != null) header("Accept", accept) }
            .build()

        return client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Fetch failed for $url: HTTP ${response.code}")
            response.body?.string().orEmpty()
        }
    }
}

/** Shopify frequently returns protocol-relative image URLs ("//cdn.shopify.com/..."). */
fun normalizeImageUrl(url: String?): String? {
    if (url == null) return null
    return if (url.startsWith("//")) "https:$url" else url
}
