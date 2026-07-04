package com.tristinbaker.vsalerts.data

import android.content.Context

private const val PREFS_NAME = "vs_alerts_settings"
private const val KEY_STOCK_THRESHOLD = "stock_threshold"
private const val KEY_ALERT_ON_PRICE_DROP = "alert_on_price_drop"
private const val KEY_ALERT_ON_SALE = "alert_on_sale"

const val DEFAULT_STOCK_THRESHOLD = 500

/** Small SharedPreferences wrapper for the handful of user-adjustable alert settings. */
class AppSettings(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var stockThreshold: Int
        get() = prefs.getInt(KEY_STOCK_THRESHOLD, DEFAULT_STOCK_THRESHOLD)
        set(value) = prefs.edit().putInt(KEY_STOCK_THRESHOLD, value).apply()

    var alertOnPriceDrop: Boolean
        get() = prefs.getBoolean(KEY_ALERT_ON_PRICE_DROP, true)
        set(value) = prefs.edit().putBoolean(KEY_ALERT_ON_PRICE_DROP, value).apply()

    var alertOnSale: Boolean
        get() = prefs.getBoolean(KEY_ALERT_ON_SALE, true)
        set(value) = prefs.edit().putBoolean(KEY_ALERT_ON_SALE, value).apply()

    companion object {
        @Volatile
        private var instance: AppSettings? = null

        fun get(context: Context): AppSettings =
            instance ?: synchronized(this) {
                instance ?: AppSettings(context.applicationContext).also { instance = it }
            }
    }
}
