package com.tristinbaker.vsalerts

import android.app.Application
import com.tristinbaker.vsalerts.notification.createNotificationChannel
import com.tristinbaker.vsalerts.worker.schedulePeriodicStockCheck

class VsAlertsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel(this)
        schedulePeriodicStockCheck(this)
    }
}
