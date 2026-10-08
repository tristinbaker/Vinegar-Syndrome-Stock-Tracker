package com.tristinbaker.vsalerts.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.io.IOException

private const val USER_AGENT =
    "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Mobile Safari/537.36"

// Shopify's bot check answers a browser User-Agent with no Accept header with a 403
// "Verifying your connection..." page, so every request must send one.
private const val ACCEPT_HTML = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
private const val ACCEPT_JSON = "application/json"

class ProductNotFoundException(handle: String) : IOException("No inventory data found for handle: $handle")

/**
 * Talks to each [Store]'s public Shopify storefront endpoints.
 * No API key: search uses the storefront predictive-search endpoint, product detail comes from
 * the `/products/{handle}.js` endpoint, and inventory is scraped from the product page.
 */
class StorefrontApi(
    private val client: OkHttpClient = OkHttpClient(),
) {
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Searches every store in parallel, Vinegar Syndrome results first. A store that fails is
     * skipped so one site being down doesn't hide the other's results; only if all fail does it throw.
     */
    suspend fun searchProducts(query: String): List<SearchProduct> = coroutineScope {
        val perStore = Store.entries.map { store ->
            async { runCatching { searchStore(store, query) } }
        }.awaitAll()

        if (perStore.all { it.isFailure }) throw perStore.first().exceptionOrNull()!!
        perStore.flatMap { it.getOrDefault(emptyList()) }
    }

    private suspend fun searchStore(store: Store, query: String): List<SearchProduct> = withContext(Dispatchers.IO) {
        val url = "${store.baseUrl}/search/suggest.json".toHttpUrl().newBuilder()
            .addQueryParameter("q", query)
            .addQueryParameter("resources[type]", "product")
            .addQueryParameter("resources[limit]", "8")
            .build()
            .toString()

        json.decodeFromString<SuggestResponse>(get(url, ACCEPT_JSON))
            .resources.results.products
            .map { it.copy(store = store) }
    }

    suspend fun fetchProduct(store: Store, handle: String): ProductDetail = withContext(Dispatchers.IO) {
        val productJson = get("${store.baseUrl}/products/$handle.js", ACCEPT_JSON)
        val product = json.decodeFromString<ProductDetail>(productJson)

        val inventory = fetchInventory(store, handle)
        product.copy(
            store = store,
            variants = product.variants.map { variant ->
                variant.copy(inventoryQuantity = inventory[variant.id])
            },
        )
    }

    /**
     * Per-variant inventory isn't in the `.js` payload, so it's scraped from the product page.
     * Vinegar Syndrome's theme exposes it as `<product-inventory data-vs-inventory="{variantId: {q, m, p}}">`;
     * Mélusine's embeds the full variant list, including `inventory_quantity`, in `<script class="product-json">`.
     */
    private fun fetchInventory(store: Store, handle: String): Map<Long, Int?> {
        val page = Jsoup.parse(get(store.productUrl(handle), ACCEPT_HTML))

        page.selectFirst("product-inventory[data-vs-inventory]")?.attr("data-vs-inventory")?.let { raw ->
            return json.decodeFromString<Map<String, VariantInventory>>(raw)
                .entries.associate { (id, inv) -> id.toLong() to inv.quantity }
        }
        page.selectFirst("script.product-json")?.data()?.let { raw ->
            return json.decodeFromString<ProductPageJson>(raw)
                .variants.associate { it.id to it.inventoryQuantity }
        }
        throw ProductNotFoundException(handle)
    }

    private fun get(url: String, accept: String): String {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .header("Accept", accept)
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
