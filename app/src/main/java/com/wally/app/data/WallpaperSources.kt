package com.wally.app.data

import com.wally.app.BuildConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import retrofit2.converter.kotlinx.serialization.asConverterFactory

interface WallpaperSource {
    val origin: WallpaperOrigin
    suspend fun fetch(query: String, page: Int): List<WallpaperItem>
}

@Serializable
data class WallhavenResponse(val data: List<WallhavenPhoto> = emptyList())

@Serializable
data class WallhavenPhoto(
    val id: String,
    val path: String? = null,
    @SerialName("dimension_x") val width: Int = 0,
    @SerialName("dimension_y") val height: Int = 0,
    val favorites: Int = 0,
)

interface WallhavenApi {
    @GET("search")
    suspend fun search(
        @Query("q") query: String? = null,
        @Query("page") page: Int,
        @Query("purity") purity: String = "100",
        @Query("sorting") sorting: String,
        @Query("atleast") minimumResolution: String = "1440x2560",
        @Query("ratios") ratios: String = "9x16,9x18,9x19.5",
        @Query("categories") categories: String = "111",
    ): WallhavenResponse
}

class WallhavenSource(private val api: WallhavenApi) : WallpaperSource {
    override val origin = WallpaperOrigin.WALLHAVEN

    override suspend fun fetch(query: String, page: Int): List<WallpaperItem> = api.search(
        query = query.takeIf { it.isNotBlank() },
        page = page,
        purity = "100",
        sorting = if (query.isBlank()) "toplist" else "relevance",
        minimumResolution = "1440x2560",
        ratios = "9x16,9x18,9x19.5",
    ).data.mapNotNull { photo ->
        val path = photo.path ?: return@mapNotNull null
        WallpaperItem(
            id = photo.id,
            title = "Wallhaven / ${photo.id}",
            imageUrl = path,
            width = photo.width,
            height = photo.height,
            origin = origin,
            qualitySignal = photo.favorites,
            tags = listOf("Wallhaven"),
        )
    }
}

@Serializable
data class UnsplashResponse(val results: List<UnsplashPhoto> = emptyList())

@Serializable
data class UnsplashPhoto(
    val id: String,
    val width: Int = 0,
    val height: Int = 0,
    val likes: Int = 0,
    val description: String? = null,
    val alt_description: String? = null,
    val urls: UnsplashUrls,
)

@Serializable
data class UnsplashUrls(val full: String? = null)

interface UnsplashApi {
    @GET("search/photos")
    suspend fun search(
        @Query("client_id") clientId: String,
        @Query("query") query: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = 24,
        @Query("orientation") orientation: String = "portrait",
        @Query("content_filter") contentFilter: String = "high",
    ): UnsplashResponse
}

class UnsplashSource(
    private val api: UnsplashApi,
    private val accessKey: String,
) : WallpaperSource {
    override val origin = WallpaperOrigin.UNSPLASH

    override suspend fun fetch(query: String, page: Int): List<WallpaperItem> {
        if (accessKey.isBlank()) return emptyList()
        return api.search(
            clientId = accessKey,
            query = query.ifBlank { "minimal vertical wallpaper" },
            page = page,
        ).results.mapNotNull { photo ->
            val imageUrl = photo.urls.full ?: return@mapNotNull null
            WallpaperItem(
                id = photo.id,
                title = photo.description ?: photo.alt_description ?: "Unsplash study",
                imageUrl = imageUrl,
                width = photo.width,
                height = photo.height,
                origin = origin,
                qualitySignal = photo.likes,
                tags = listOf("Photography", "Unsplash"),
            )
        }
    }
}

@Serializable
data class PexelsResponse(val photos: List<PexelsPhoto> = emptyList())

@Serializable
data class PexelsPhoto(
    val id: Long,
    val width: Int = 0,
    val height: Int = 0,
    val alt: String? = null,
    val src: PexelsSources,
)

@Serializable
data class PexelsSources(
    val original: String? = null,
    @SerialName("large2x") val large2x: String? = null,
)

interface PexelsApi {
    @GET("v1/search")
    suspend fun search(
        @Header("Authorization") apiKey: String,
        @Query("query") query: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = 24,
        @Query("orientation") orientation: String = "portrait",
    ): PexelsResponse
}

class PexelsSource(
    private val api: PexelsApi,
    private val apiKey: String,
) : WallpaperSource {
    override val origin = WallpaperOrigin.PEXELS

    override suspend fun fetch(query: String, page: Int): List<WallpaperItem> {
        if (apiKey.isBlank()) return emptyList()
        return api.search(
            apiKey = apiKey,
            query = query.ifBlank { "minimal vertical wallpaper" },
            page = page,
        ).photos.mapNotNull { photo ->
            val imageUrl = photo.src.original ?: photo.src.large2x ?: return@mapNotNull null
            WallpaperItem(
                id = photo.id.toString(),
                title = photo.alt ?: "Pexels study",
                imageUrl = imageUrl,
                width = photo.width,
                height = photo.height,
                origin = origin,
                tags = listOf("Photography", "Pexels"),
            )
        }
    }
}

@Serializable
data class PixabayResponse(val hits: List<PixabayPhoto> = emptyList())

@Serializable
data class PixabayPhoto(
    val id: Long,
    @SerialName("imageWidth") val width: Int = 0,
    @SerialName("imageHeight") val height: Int = 0,
    val likes: Int = 0,
    val downloads: Int = 0,
    @SerialName("fullHDURL") val fullHdUrl: String? = null,
    @SerialName("largeImageURL") val largeImageUrl: String? = null,
)

interface PixabayApi {
    @GET("api/")
    suspend fun search(
        @Query("key") apiKey: String,
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("per_page") perPage: Int = 24,
        @Query("image_type") imageType: String = "photo",
        @Query("orientation") orientation: String = "vertical",
        @Query("min_width") minimumWidth: Int = 1440,
        @Query("safesearch") safeSearch: Boolean = true,
    ): PixabayResponse
}

class PixabaySource(
    private val api: PixabayApi,
    private val apiKey: String,
) : WallpaperSource {
    override val origin = WallpaperOrigin.PIXABAY

    override suspend fun fetch(query: String, page: Int): List<WallpaperItem> {
        if (apiKey.isBlank()) return emptyList()
        return api.search(
            apiKey = apiKey,
            query = query.ifBlank { "minimal vertical wallpaper" },
            page = page,
        ).hits.mapNotNull { photo ->
            val imageUrl = photo.fullHdUrl ?: photo.largeImageUrl ?: return@mapNotNull null
            WallpaperItem(
                id = photo.id.toString(),
                title = "Pixabay / ${photo.id}",
                imageUrl = imageUrl,
                width = photo.width,
                height = photo.height,
                origin = origin,
                qualitySignal = maxOf(photo.likes, photo.downloads / 100),
                tags = listOf("Photography", "Pixabay"),
            )
        }
    }
}

object RetrofitFactory {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    fun client(httpClient: OkHttpClient, baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(httpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
}

class MergedWallpaperRepository(
    private val sources: List<WallpaperSource>,
) {
    val curated: List<WallpaperItem> = FallbackCatalog.items

    suspend fun page(query: String, page: Int): List<WallpaperItem> =
        kotlinx.coroutines.supervisorScope {
            val responses = sources.map { source ->
                kotlinx.coroutines.async {
                    runCatching { source.fetch(query, page) }.getOrDefault(emptyList())
                }
            }.map { it.await() }

            val seen = LinkedHashSet<String>()
            val merged = responses
                .flatten()
                .filter(QualityGate::accepts)
                .filter { item ->
                    // IDs are the stable cross-session key. The URL check catches provider aliases.
                    val sourceKey = item.stableId
                    val urlKey = item.imageUrl.substringBefore('?').lowercase()
                    seen.add(sourceKey) && seen.add("url:$urlKey")
                }

            val fallback = if (page == 1) {
                curated.filter { item ->
                    query.isBlank() || item.tags.any { tag -> tag.equals(query, ignoreCase = true) } ||
                        item.title.contains(query, ignoreCase = true)
                }
            } else {
                emptyList()
            }

            (merged + fallback.filter { item ->
                val key = item.stableId
                val urlKey = "url:${item.imageUrl.substringBefore('?').lowercase()}"
                seen.add(key) && seen.add(urlKey)
            }).distinctBy { it.stableId }
        }
}

object NetworkSources {
    fun create(httpClient: OkHttpClient): List<WallpaperSource> {
        val wallhaven = RetrofitFactory.client(httpClient, "https://wallhaven.cc/api/v1/")
            .create(WallhavenApi::class.java)
        val unsplash = RetrofitFactory.client(httpClient, "https://api.unsplash.com/")
            .create(UnsplashApi::class.java)
        val pexels = RetrofitFactory.client(httpClient, "https://api.pexels.com/")
            .create(PexelsApi::class.java)
        val pixabay = RetrofitFactory.client(httpClient, "https://pixabay.com/")
            .create(PixabayApi::class.java)

        // List order is intentional: quality-wallpaper-specific sources win ties in the merge.
        return listOf(
            WallhavenSource(wallhaven),
            UnsplashSource(unsplash, BuildConfig.UNSPLASH_ACCESS_KEY),
            PexelsSource(pexels, BuildConfig.PEXELS_API_KEY),
            PixabaySource(pixabay, BuildConfig.PIXABAY_API_KEY),
        )
    }
}
