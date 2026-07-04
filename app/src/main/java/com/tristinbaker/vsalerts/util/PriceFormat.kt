package com.tristinbaker.vsalerts.util

fun formatCents(cents: Int): String {
    val dollars = cents / 100
    val remainder = cents % 100
    return "$%d.%02d".format(dollars, remainder)
}
