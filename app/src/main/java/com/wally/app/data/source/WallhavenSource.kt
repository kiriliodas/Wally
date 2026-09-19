package com.wally.app.data.source

import com.wally.app.data.api.WallhavenApiService
import com.wally.app.data.model.WallpaperItem
import com.wally.app.data.model.WallpaperSourceType
import java.security.MessageDigest

class WallhavenSource(
    private val apiService: WallhavenApiService,
    private val apiKey: String? = null
) : WallpaperSource {

    override val sourceType: WallpaperSourceType = WallpaperSourceType.WALLHAVEN
    override val priorityWeight: Int = 1 // Highest priority
    override val isConfigured: Boolean = true // Free public SFW endpoint does not require key

    override suspend fun fetchWallpapers(query: String?, category: String?, page: Int): List<WallpaperItem> {
        val q = query ?: category ?: ""
        val sorting = if (q.isBlank()) "toplist" else "relevance"
        val response = apiService.searchWallpapers(
            query = if (q.isBlank()) null else q,
            categories = "111", // General + Anime + People
            purity = "100",    // SFW only
            sorting = sorting,
            order = "desc",
            atleast = "1440x2560",
            ratios = "9x16,9x18,9x19.5",
            page = page,
            apiKey = if (apiKey.isNullOrBlank()) null else apiKey
        )

        return response.data.map { img ->
            val preview = if (img.thumbs.large.isNotBlank()) img.thumbs.large else img.thumbs.small
            val hash = generatePerceptualFingerprint("wallhaven_${img.id}")
            val color = img.colors.firstOrNull()?.let { if (it.startsWith("#")) it else "#$it" }
            WallpaperItem(
                id = img.id,
                source = WallpaperSourceType.WALLHAVEN,
                fullUrl = img.path,
                previewUrl = preview,
                width = img.dimensionX,
                height = img.dimensionY,
                author = img.uploader?.username ?: "Wallhaven Artist",
                authorUrl = "https://wallhaven.cc/w/${img.id}",
                likesOrFavorites = img.favorites,
                views = img.views,
                tags = listOf("wallhaven", img.ratio),
                perceptualHash = hash,
                colorHex = color
            )
        }
    }

    private fun generatePerceptualFingerprint(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val bytes = md.digest(input.toByteArray())
        return bytes.take(8).joinToString("") { "%02x".format(it) }
    }
}
