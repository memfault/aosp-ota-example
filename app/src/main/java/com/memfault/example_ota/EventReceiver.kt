package com.memfault.example_ota

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.*
import java.util.concurrent.TimeUnit

internal const val CHECK_FOR_UPDATE_WORK_NAME = "com.memfault.ota.check"

class EventReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        intent ?: return
        Logger.v("Handling action=${intent.action}")

        when (intent.action) {
            "android.intent.action.BOOT_COMPLETED" ->
                PeriodicWorkRequestBuilder<CheckForUpdateWorker>(
                    BuildConfig.CHECK_FOR_UPDATE_INTERVAL_HOURS.toLong(),
                    TimeUnit.HOURS
                ).setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                    .build()
                    .also {
                        WorkManager.getInstance(context)
                            .enqueueUniquePeriodicWork(
                                CHECK_FOR_UPDATE_WORK_NAME,
                                ExistingPeriodicWorkPolicy.REPLACE,
                                it
                            )
                    }
            "android.intent.action.MAIN" -> OneTimeWorkRequestBuilder<CheckForUpdateWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
                .also {
                    WorkManager.getInstance(context)
                        .enqueue(it)
                }
        }
    }
}
