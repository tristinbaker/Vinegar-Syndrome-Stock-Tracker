package com.tristinbaker.vsalerts.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.tristinbaker.vsalerts.data.AppSettings

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val settings = AppSettings.get(application)

    var stockThresholdText by mutableStateOf(settings.stockThreshold.toString())
        private set
    var alertOnPriceDrop by mutableStateOf(settings.alertOnPriceDrop)
        private set
    var alertOnSale by mutableStateOf(settings.alertOnSale)
        private set

    fun onStockThresholdChange(text: String) {
        stockThresholdText = text
        text.toIntOrNull()?.let { settings.stockThreshold = it }
    }

    fun onAlertOnPriceDropChange(enabled: Boolean) {
        alertOnPriceDrop = enabled
        settings.alertOnPriceDrop = enabled
    }

    fun onAlertOnSaleChange(enabled: Boolean) {
        alertOnSale = enabled
        settings.alertOnSale = enabled
    }
}
