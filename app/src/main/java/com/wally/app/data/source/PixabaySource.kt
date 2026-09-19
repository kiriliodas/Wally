package com.wally.app.data.source

import com.wally.app.data.api.PixabayApiService
import com.wally.app.data.model.WallpaperItem
import com.wally.app.data.model.WallpaperSourceType
import java.security.MessageDigest

class PixabaySource(
    private val apiService: PixabayApiService,
    private val apiKey: String
) : WallpaperSource {

    override val sourceType: WallpaperSourceType = WallpaperSourceType.PIXABAY
    override val priorityWeight: Int = 4
    override val isConfigured: Boolean = apiKey.isNotBlank()

    override suspend fun fetchWallpapers(query: String?, category: String?, page: Int): List<WallpaperItem> {
        if (!isConfigured) return emptyList()

        val q = query ?: category ?: "dark vertical wallpaper"
        val response = apiService.searchPhotos(
            apiKey = apiKey,
            query = q,
            imageType = "photo",
            orientation = "vertical",
            minWidth = 1440,
            page = page,
            perPage = 20
        )

        val minPixelFloor = 1440L * 2560L

        return response.hits
            .filter { (it.imageWidth.toLong() * it.imageHeight.toLong()) >= minPixelFloor }
            .map { hit ->
                val fullUrl = hit.fullHDURL ?: hit.imageURL ?: hit.largeImageURL
                val previewUrl = hit.webformatURL.ifBlank { hit.largeImageURL }
                val hash = generatePerceptualFingerprint("pixabay_${hit.id}")
                WallpaperItem(
                    id = hit.id.toString(),
                    source = WallpaperSourceType.PIXABAY,
                    fullUrl = fullUrl,
                    previewUrl = previewUrl,
                    width = hit.imageWidth,
                    height = hit.imageHeight,
                    author = hit.user,
                    authorUrl = hit.pageURL,
                    likesOrFavorites = hit.likes,
                    views = hit.views,
                    tags = hit.tags.split(",").map { it.trim() },
                    perceptualHash = hash
                )
            }
    }

    private fun generatePerceptualFingerprint(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val bytes = md.digest(input.toByteArray())
        return bytes.take(8).joinToString("") { "%02x".format(it) }
    }
}
