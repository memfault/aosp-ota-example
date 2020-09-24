package com.memfault.example_ota

import android.content.Context
import android.os.RecoverySystem
import com.memfault.cloud.sdk.MemfaultOtaPackage
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okio.BufferedSink
import okio.buffer
import okio.sink
import java.io.File
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class UnexpectedResponseException(message: String): Exception(message)

internal suspend fun downloadOtaPackage(otaPackage: MemfaultOtaPackage, targetFile: File) {
    Logger.i("Downloading ${otaPackage.location}...")
    val request = Request.Builder().url(otaPackage.location).build()

    // Even though in a coroutine, let's avoid calling a blocking function (execute) and instead
    // use the OkHttp thread pool:

    suspendCancellableCoroutine<Unit> { cont ->
        okHttp.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Logger.e("Download failed", e)
                cont.resumeWithException(e)
            }
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    it.body?.let { body ->
                        targetFile.sink().buffer().use { sink ->
                            sink.writeAll(body.source())
                        }
                        return cont.resume(Unit)
                    }
                }

                return cont.resumeWithException(UnexpectedResponseException("Unexpected null body"))
            }
        })
    }
}

internal fun verifyOtaPackage(packageFile: File): Boolean {
    Logger.i("Verifying $packageFile...")
    val progressLogger = RecoverySystem.ProgressListener {
            percent -> Logger.i("Verifying $packageFile ($percent%)...")
    }
    try {
        RecoverySystem.verifyPackage(packageFile, progressLogger, null)
    } catch (e: Exception) {
        Logger.e("Verification failure", e)
        return false
    }
    Logger.i("Verification OK")
    return true
}

internal fun installOtaPackage(context: Context, packageFile: File) {
    Logger.i("Installing $packageFile...")
    try {
        RecoverySystem.installPackage(context, packageFile)
    } catch (e: Exception) {
        Logger.e("Install failure", e)
    }
    Logger.e("Install OK")
}


internal suspend fun installUpdate(context: Context, otaPackage: MemfaultOtaPackage) {
    Logger.d("installUpdate: $otaPackage")

    val packageFile = provideDownloadLocation(context)
    if (packageFile.exists()) {
        packageFile.delete()
    }
    downloadOtaPackage(otaPackage, packageFile)
    if (verifyOtaPackage(packageFile)) {
        installOtaPackage(context, packageFile)
    }
}
