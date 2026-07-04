package com.tristinbaker.vsalerts.util

private const val STOCK_DISPLAY_CAP = 2000

fun formatStockLabel(quantity: Int): String =
    if (quantity > STOCK_DISPLAY_CAP) ">$STOCK_DISPLAY_CAP in stock" else "$quantity in stock"
