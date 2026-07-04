package com.tristinbaker.vsalerts.util

/** Vinegar Syndrome's imprints, in the order the collection should group/sort by. */
val VENDOR_LABEL_ORDER = listOf(
    "Vinegar Syndrome",
    "Vinegar Syndrome Archive",
    "Vinegar Syndrome Labs",
    "Vinegar Syndrome Pictures",
    "Vinegar Syndrome Ultra",
    "Cinématographe",
    "Degausser Video",
    "Distribpix",
    "Iconoscope",
    "Pink Line",
    "Reviver",
)

/** Labels Shopify returns that aren't in [VENDOR_LABEL_ORDER] sort after all known ones, alphabetically. */
fun vendorSortIndex(label: String): Int {
    val index = VENDOR_LABEL_ORDER.indexOf(label)
    return if (index >= 0) index else VENDOR_LABEL_ORDER.size
}
