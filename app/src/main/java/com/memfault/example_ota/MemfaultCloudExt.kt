package com.memfault.example_ota

import com.memfault.cloud.sdk.GetLatestReleaseCallback
import com.memfault.cloud.sdk.MemfaultCloud
import com.memfault.cloud.sdk.MemfaultDeviceInfo
import com.memfault.cloud.sdk.MemfaultOtaPackage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun MemfaultCloud.getLatestRelease(deviceInfo: MemfaultDeviceInfo): MemfaultOtaPackage? =
    suspendCancellableCoroutine { cont ->
        getLatestRelease(deviceInfo, object : GetLatestReleaseCallback {
            override fun onError(e: Exception) {
                cont.resumeWithException(e)
            }
            override fun onUpToDate() {
                cont.resume(null)
            }
            override fun onUpdateAvailable(otaPackage: MemfaultOtaPackage) {
                cont.resume(otaPackage)
            }
        })
    }
