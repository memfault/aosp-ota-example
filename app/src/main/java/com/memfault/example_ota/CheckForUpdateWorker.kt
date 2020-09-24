package com.memfault.example_ota

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import com.memfault.cloud.sdk.MemfaultCloud
import com.memfault.cloud.sdk.MemfaultDeviceInfo
import com.memfault.cloud.sdk.MemfaultOtaPackage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal const val SOFTWARE_TYPE_ANDROID_BUILD = "android-build"

internal suspend fun checkForUpdate(
    cloud: MemfaultCloud,
    deviceInfo: MemfaultDeviceInfo,
    handleUpdate: suspend (MemfaultOtaPackage) -> Unit
): Result =
    try {
        Logger.i("Checking for update...")
        val otaPackage = cloud.getLatestRelease(deviceInfo)
        if (otaPackage == null) {
            Logger.i("Up to date!")
        } else {
            Logger.i("Update available: ${otaPackage.appVersion}")
            handleUpdate(otaPackage)
        }
        Result.success()
    } catch (e: Exception) {
        Logger.e("getLatestRelease() failed", e)
        Result.failure()
    }

@SuppressLint("MissingPermission")
internal fun getDeviceInfo() =
    MemfaultDeviceInfo(
        deviceSerial = Build.getSerial(),
        currentVersion = Build.FINGERPRINT,
        hardwareVersion = Build.BOARD,
        softwareType = SOFTWARE_TYPE_ANDROID_BUILD
    )

internal class CheckForUpdateWorker(
    appContext: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Logger.v("CheckForUpdateWorker doWork()")
            checkForUpdate(
                cloud = memfaultCloud,
                deviceInfo = getDeviceInfo(),
                handleUpdate = { otaPackage -> installUpdate(applicationContext, otaPackage)}
            )
        }

}
