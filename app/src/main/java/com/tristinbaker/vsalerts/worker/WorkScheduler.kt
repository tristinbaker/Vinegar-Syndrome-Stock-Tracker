package com.tristinbaker.vsalerts.worker

import android.content.Context
import androidx.work.BackoffPolicy
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

// A rate-limited run returns Result.retry(). The stores send Retry-After: 60, but retrying
// after a minute was still refused, so back off from 5 minutes instead of WorkManager's 30s default.
private const val RATE_LIMIT_BACKOFF_MINUTES = 5L

/** Schedules the recurring background check; safe to call every app start (keeps existing schedule). */
fun schedulePeriodicStockCheck(context: Context) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val request = PeriodicWorkRequestBuilder<StockCheckWorker>(30, TimeUnit.MINUTES)
        .setConstraints(constraints)
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, RATE_LIMIT_BACKOFF_MINUTES, TimeUnit.MINUTES)
        .build()

    // UPDATE rather than KEEP so installs that already have the job scheduled pick up the
    // backoff settings; it still keeps the existing schedule's timing.
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        PERIODIC_WORK_NAME,
        ExistingPeriodicWorkPolicy.UPDATE,
        request,
    )
}

/** Triggered by pull-to-refresh / "check now" so the user isn't stuck waiting on the schedule. */
fun runStockCheckNow(context: Context) {
    val request = OneTimeWorkRequestBuilder<StockCheckWorker>()
        .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, RATE_LIMIT_BACKOFF_MINUTES, TimeUnit.MINUTES)
        .build()
    WorkManager.getInstance(context).enqueueUniqueWork(
        ONE_TIME_WORK_NAME,
        ExistingWorkPolicy.KEEP,
        request,
    )
}
