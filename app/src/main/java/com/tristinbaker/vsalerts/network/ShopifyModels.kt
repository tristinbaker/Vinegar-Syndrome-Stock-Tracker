package com.tristinbaker.vsalerts.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class SuggestResponse(
    val resources: SuggestResources = SuggestResources(),
)

@Serializable
data class SuggestResources(
    val results: SuggestResults = SuggestResults(),
)

@Serializable
data class SuggestResults(
    val products: List<SearchProduct> = emptyList(),
)

@Serializable
data class SearchProduct(
    val handle: String,
    val title: String,
    val vendor: String? = null,
    val price: String? = null,
    @SerialName("price_min") val priceMin: String? = null,
    @SerialName("price_max") val priceMax: String? = null,
    @SerialName("compare_at_price_min") val compareAtPriceMin: String? = null,
    @SerialName("compare_at_price_max") val compareAtPriceMax: String? = null,
    val image: String? = null,
    // Not in the response; set by the API from whichever store returned the result.
    @Transient val store: Store = Store.VINEGAR_SYNDROME,
)

@Serializable
data class ProductDetail(
    val id: Long,
    val title: String,
    val handle: String,
    val vendor: String? = null,
    val variants: List<ProductVariant> = emptyList(),
    // Product-level fallback image; single-variant products often leave the variant's own
    // featured_image null and rely on this instead.
    @SerialName("featured_image") val featuredImage: String? = null,
    @Transient val store: Store = Store.VINEGAR_SYNDROME,
)

@Serializable
data class ProductVariant(
    val id: Long,
    val title: String,
    val price: Int,
    @SerialName("compare_at_price") val compareAtPrice: Int? = null,
    val available: Boolean = false,
    @SerialName("inventory_management") val inventoryManagement: String? = null,
    @SerialName("inventory_quantity") val inventoryQuantity: Int? = null,
    @SerialName("featured_image") val featuredImage: FeaturedImage? = null,
)

@Serializable
data class FeaturedImage(
    val src: String? = null,
)

/** One entry of the product page's `data-vs-inventory` map, keyed by variant id. */
@Serializable
data class VariantInventory(
    @SerialName("q") val quantity: Int? = null,
)

/** The `<script class="product-json">` blob some themes (e.g. Mélusine's) embed on product pages. */
@Serializable
data class ProductPageJson(
    val variants: List<ProductPageVariant> = emptyList(),
)

@Serializable
data class ProductPageVariant(
    val id: Long,
    @SerialName("inventory_quantity") val inventoryQuantity: Int? = null,
)
