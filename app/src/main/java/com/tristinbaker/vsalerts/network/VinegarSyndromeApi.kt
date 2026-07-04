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

class ProductNotFoundException(handle: String) : IOException("No product-json found for handle: $handle")

/**
 * Talks to vinegarsyndrome.com's public Shopify storefront endpoints.
 * No API key: search uses the storefront predictive-search endpoint, product
 * detail is scraped from the `product-json` script tag embedded in the product page.
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
        val request = Request.Builder()
            .url("$BASE_URL/products/$handle")
            .header("User-Agent", USER_AGENT)
            .build()

        val html = client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Product fetch failed: HTTP ${response.code}")
            response.body?.string().orEmpty()
        }

        val doc = Jsoup.parse(html)
        val scriptJson = doc.selectFirst("script.product-json")?.data()
            ?: throw ProductNotFoundException(handle)

        json.decodeFromString<ProductDetail>(scriptJson)
    }
}

/** Shopify frequently returns protocol-relative image URLs ("//cdn.shopify.com/..."). */
fun normalizeImageUrl(url: String?): String? {
    if (url == null) return null
    return if (url.startsWith("//")) "https:$url" else url
}
