package com.wally.app.data.source

import com.wally.app.data.api.PexelsApiService
import com.wally.app.data.model.WallpaperItem
import com.wally.app.data.model.WallpaperSourceType
import java.security.MessageDigest

class PexelsSource(
    private val apiService: PexelsApiService,
    private val apiKey: String
) : WallpaperSource {

    override val sourceType: WallpaperSourceType = WallpaperSourceType.PEXELS
    override val priorityWeight: Int = 3
    override val isConfigured: Boolean = apiKey.isNotBlank()

    override suspend fun fetchWallpapers(query: String?, category: String?, page: Int): List<WallpaperItem> {
        if (!isConfigured) return emptyList()

        val q = query ?: category ?: "dark wallpaper 4k"
        val response = apiService.searchPhotos(
            query = q,
            page = page,
            perPage = 20,
            orientation = "portrait",
            size = "large",
            apiKey = apiKey
        )

        return response.photos
            .filter { it.width >= 1440 }
            .map { photo ->
                val fullUrl = photo.src.original.ifBlank { photo.src.large2x }
                val previewUrl = photo.src.large2x.ifBlank { photo.src.portrait }
                val hash = generatePerceptualFingerprint("pexels_${photo.id}")
                WallpaperItem(
                    id = photo.id.toString(),
                    source = WallpaperSourceType.PEXELS,
                    fullUrl = fullUrl,
                    previewUrl = previewUrl,
                    width = photo.width,
                    height = photo.height,
                    author = photo.photographer,
                    authorUrl = photo.photographerUrl,
                    likesOrFavorites = 10, // Pexels curated API provides high baseline quality
                    perceptualHash = hash,
                    colorHex = photo.avgColor
                )
            }
    }

    private fun generatePerceptualFingerprint(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val bytes = md.digest(input.toByteArray())
        return bytes.take(8).joinToString("") { "%02x".format(it) }
    }
}
