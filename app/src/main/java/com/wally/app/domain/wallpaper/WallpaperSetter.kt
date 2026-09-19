package com.wally.app.domain.wallpaper

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.InputStream

enum class WallpaperTarget(val label: String) {
    HOME("Home Screen"),
    LOCK("Lock Screen"),
    BOTH("Home & Lock Screen")
}

sealed interface WallpaperSetState {
    object Idle : WallpaperSetState
    object Applying : WallpaperSetState
    object Success : WallpaperSetState
    data class Error(val message: String) : WallpaperSetState
}

class WallpaperSetter(private val okHttpClient: OkHttpClient) {

    suspend fun applyWallpaper(
        context: Context,
        imageUrl: String,
        target: WallpaperTarget
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder().url(imageUrl).build()
            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                throw IllegalStateException("Failed to download wallpaper: HTTP ${response.code}")
            }

            val body = response.body ?: throw IllegalStateException("Empty response body")
            val inputStream: InputStream = body.byteStream()

            val wallpaperManager = WallpaperManager.getInstance(context)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val whichFlag = when (target) {
                    WallpaperTarget.HOME -> WallpaperManager.FLAG_SYSTEM
                    WallpaperTarget.LOCK -> WallpaperManager.FLAG_LOCK
                    WallpaperTarget.BOTH -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                }
                wallpaperManager.setStream(inputStream, null, true, whichFlag)
            } else {
                val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
                wallpaperManager.setBitmap(bitmap)
            }
        }
    }
}
