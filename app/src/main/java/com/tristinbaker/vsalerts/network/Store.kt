package com.tristinbaker.vsalerts.network

/**
 * The Shopify storefronts the app can search and track. Persisted by enum name in Room, so
 * existing constants must never be renamed.
 */
enum class Store(val displayName: String, val baseUrl: String) {
    VINEGAR_SYNDROME("Vinegar Syndrome", "https://vinegarsyndrome.com"),
    MELUSINE("Mélusine", "https://melusine.com"),
    ;

    fun productUrl(handle: String): String = "$baseUrl/products/$handle"
}
