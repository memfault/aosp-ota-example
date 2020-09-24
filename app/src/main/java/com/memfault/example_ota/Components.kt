package com.memfault.example_ota

import android.content.Context
import com.memfault.cloud.sdk.MemfaultCloud
import okhttp3.OkHttpClient
import java.io.File

val memfaultCloud = MemfaultCloud.Builder()
    .setApiKey(BuildConfig.MEMFAULT_PROJECT_API_KEY).apply {
        baseApiUrl = BuildConfig.MEMFAULT_API_BASE_URL
    }
    .build()

val okHttp = OkHttpClient.Builder().build()

// [MFLT] Configure your desired download location
// Ensure that you download the OTA package to a location with sufficient storage.

fun provideDownloadLocation(context: Context) =
    File(context.cacheDir, "ota-update.zip")
