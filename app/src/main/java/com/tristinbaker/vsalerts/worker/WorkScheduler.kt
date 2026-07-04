package com.tristinbaker.vsalerts.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

private const val PERIODIC_WORK_NAME = "stock_check_periodic"
private const val ONE_TIME_WORK_NAME = "stock_check_now"

/** Schedules the recurring background check; safe to call every app start (keeps existing schedule). */
fun schedulePeriodicStockCheck(context: Context) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val request = PeriodicWorkRequestBuilder<StockCheckWorker>(30, TimeUnit.MINUTES)
        .setConstraints(constraints)
        .build()

    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        PERIODIC_WORK_NAME,
        ExistingPeriodicWorkPolicy.KEEP,
        request,
    )
}

/** Triggered by pull-to-refresh / "check now" so the user isn't stuck waiting on the schedule. */
fun runStockCheckNow(context: Context) {
    val request = OneTimeWorkRequestBuilder<StockCheckWorker>().build()
    WorkManager.getInstance(context).enqueueUniqueWork(
        ONE_TIME_WORK_NAME,
        ExistingWorkPolicy.KEEP,
        request,
    )
}
