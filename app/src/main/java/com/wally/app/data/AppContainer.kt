package com.wally.app.data

import android.app.WallpaperManager
import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.net.HttpURLConnection
import java.net.URI

private val Context.wallyPreferences by preferencesDataStore(name = "wally_preferences")

class SavedWallpaperStore(private val context: Context) {
    private object Keys {
        val savedIds = stringSetPreferencesKey("saved_wallpaper_ids")
        val lastCategory = stringPreferencesKey("last_category")
    }

    val savedIds: Flow<Set<String>> = context.wallyPreferences.data.map { preferences ->
        preferences[Keys.savedIds] ?: emptySet()
    }

    val lastCategory: Flow<String> = context.wallyPreferences.data.map { preferences ->
        preferences[Keys.lastCategory] ?: "All"
    }

    suspend fun toggle(id: String) {
        context.wallyPreferences.edit { preferences ->
            val ids = (preferences[Keys.savedIds] ?: emptySet()).toMutableSet()
            if (!ids.add(id)) ids.remove(id)
            preferences[Keys.savedIds] = ids
        }
    }

    suspend fun setLastCategory(category: String) {
        context.wallyPreferences.edit { preferences -> preferences[Keys.lastCategory] = category }
    }
}

class WallpaperApplier(private val context: Context) {
    suspend fun set(item: WallpaperItem): Result<Unit> = runCatching {
        val temporary = File.createTempFile("wally-wallpaper-", ".image", context.cacheDir)
        try {
            temporary.outputStream().use { output -> openInput(item).use { input -> input.copyTo(output) } }
            temporary.inputStream().use { stream ->
                WallpaperManager.getInstance(context).setStream(
                    stream,
                    null,
                    true,
                    WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK,
                )
            }
        } finally {
            temporary.delete()
        }
    }

    private fun openInput(item: WallpaperItem): java.io.InputStream {
        if (item.imageUrl.startsWith("content://")) {
            return context.contentResolver.openInputStream(URI(item.imageUrl).toContentUri())
                ?: error("The selected image could not be opened")
        }
        val connection = (java.net.URL(item.imageUrl).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 30_000
            requestMethod = "GET"
            setRequestProperty("User-Agent", "Wally/1.0 (Android wallpaper app)")
            connect()
        }
        if (connection.responseCode !in 200..299) {
            connection.disconnect()
            error("Wallpaper download failed (${connection.responseCode})")
        }
        return connection.inputStream
    }
}

/** java.net.URI is used only to avoid Uri parsing imports in the network-focused class. */
private fun URI.toContentUri(): android.net.Uri = android.net.Uri.parse(toString())

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val httpClient = OkHttpClient.Builder()
        .cache(Cache(File(appContext.cacheDir, "api-metadata"), 8L * 1024L * 1024L))
        .build()

    val repository = MergedWallpaperRepository(NetworkSources.create(httpClient))
    val savedStore = SavedWallpaperStore(appContext)
    val wallpaperApplier = WallpaperApplier(appContext)
}

class WallyApplication : android.app.Application(), coil.ImageLoaderFactory {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }

    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this)
        .memoryCache {
            MemoryCache.Builder(this)
                .maxSizePercent(0.25)
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(File(cacheDir, "wallpaper-images"))
                .maxSizeBytes(150L * 1024L * 1024L)
                .build()
        }
        .respectCacheHeaders(false)
        .crossfade(true)
        .build()
}
