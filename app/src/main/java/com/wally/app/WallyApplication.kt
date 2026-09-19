package com.wally.app

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.wally.app.data.api.PexelsApiService
import com.wally.app.data.api.PixabayApiService
import com.wally.app.data.api.UnsplashApiService
import com.wally.app.data.api.WallhavenApiService
import com.wally.app.data.model.WallpaperSourceType
import com.wally.app.data.persistence.SavedWallpapersStorage
import com.wally.app.data.quality.QualityGate
import com.wally.app.data.repository.WallpaperRepository
import com.wally.app.data.source.PexelsSource
import com.wally.app.data.source.PixabaySource
import com.wally.app.data.source.UnsplashSource
import com.wally.app.data.source.WallhavenSource
import com.wally.app.domain.wallpaper.WallpaperSetter
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

class AppContainer(private val application: Application) {

    // Network Client
    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val jsonMediaType = "application/json".toMediaType()

    // Wallhaven API & Source
    val wallhavenSource: WallhavenSource by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://wallhaven.cc/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(jsonMediaType))
            .build()
        val service = retrofit.create(WallhavenApiService::class.java)
        WallhavenSource(service, BuildConfig.WALLHAVEN_API_KEY)
    }

    // Unsplash API & Source
    val unsplashSource: UnsplashSource by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.unsplash.com/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(jsonMediaType))
            .build()
        val service = retrofit.create(UnsplashApiService::class.java)
        UnsplashSource(service, BuildConfig.UNSPLASH_ACCESS_KEY)
    }

    // Pexels API & Source
    val pexelsSource: PexelsSource by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.pexels.com/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(jsonMediaType))
            .build()
        val service = retrofit.create(PexelsApiService::class.java)
        PexelsSource(service, BuildConfig.PEXELS_API_KEY)
    }

    // Pixabay API & Source
    val pixabaySource: PixabaySource by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://pixabay.com/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(jsonMediaType))
            .build()
        val service = retrofit.create(PixabayApiService::class.java)
        PixabaySource(service, BuildConfig.PIXABAY_API_KEY)
    }

    // Quality Gate
    val qualityGate: QualityGate by lazy {
        QualityGate(
            minLongEdge = 2560,
            minAspectRatio = 0.40f,
            maxAspectRatio = 0.62f,
            wallhavenMinFavorites = 5,
            unsplashMinLikes = 5,
            pixabayMinLikes = 5
        )
    }

    // Merged Repository
    val wallpaperRepository: WallpaperRepository by lazy {
        WallpaperRepository(
            sources = listOf(wallhavenSource, unsplashSource, pexelsSource, pixabaySource),
            qualityGate = qualityGate
        )
    }

    // Local DataStore Persistence
    val savedStorage: SavedWallpapersStorage by lazy {
        SavedWallpapersStorage(application)
    }

    // Domain Wallpaper Setter
    val wallpaperSetter: WallpaperSetter by lazy {
        WallpaperSetter(okHttpClient)
    }

    val sourcesConfiguredMap: Map<WallpaperSourceType, Boolean> by lazy {
        mapOf(
            WallpaperSourceType.WALLHAVEN to wallhavenSource.isConfigured,
            WallpaperSourceType.UNSPLASH to unsplashSource.isConfigured,
            WallpaperSourceType.PEXELS to pexelsSource.isConfigured,
            WallpaperSourceType.PIXABAY to pixabaySource.isConfigured
        )
    }
}

class WallyApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        // Configure Coil image loading to strictly meet prompt constraints:
        // - Disk cache capped at 150MB
        // - In-memory cache capped at 25% of app memory class
        val imageLoader = ImageLoader.Builder(this)
            .okHttpClient(container.okHttpClient)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("wally_image_cache"))
                    .maxSizeBytes(150L * 1024 * 1024) // 150 MB hard budget
                    .build()
            }
            .respectCacheHeaders(false)
            .build()

        Coil.setImageLoader(imageLoader)
    }
}
