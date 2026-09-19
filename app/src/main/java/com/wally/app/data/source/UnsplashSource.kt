package com.wally.app.data.source

import com.wally.app.data.api.UnsplashApiService
import com.wally.app.data.model.WallpaperItem
import com.wally.app.data.model.WallpaperSourceType
import java.security.MessageDigest

class UnsplashSource(
    private val apiService: UnsplashApiService,
    private val accessKey: String
) : WallpaperSource {

    override val sourceType: WallpaperSourceType = WallpaperSourceType.UNSPLASH
    override val priorityWeight: Int = 2
    override val isConfigured: Boolean = accessKey.isNotBlank()

    override suspend fun fetchWallpapers(query: String?, category: String?, page: Int): List<WallpaperItem> {
        if (!isConfigured) return emptyList()

        val q = query ?: category ?: "dark minimal wallpaper"
        val response = apiService.searchPhotos(
            query = q,
            page = page,
            perPage = 20,
            orientation = "portrait",
            authorization = "Client-ID $accessKey"
        )

        return response.results
            .filter { it.width >= 1440 } // Hard-filter width >= 1440
            .map { photo ->
                val fullUrl = photo.urls.full // Never thumb/small
                val previewUrl = photo.urls.regular.ifBlank { photo.urls.full }
                val hash = generatePerceptualFingerprint("unsplash_${photo.id}")
                WallpaperItem(
                    id = photo.id,
                    source = WallpaperSourceType.UNSPLASH,
                    fullUrl = fullUrl,
                    previewUrl = previewUrl,
                    width = photo.width,
                    height = photo.height,
                    author = photo.user.name,
                    authorUrl = photo.user.links?.html,
                    likesOrFavorites = photo.likes,
                    tags = listOfNotNull(photo.description?.take(30)),
                    perceptualHash = hash,
                    colorHex = photo.color
                )
            }
    }

    private fun generatePerceptualFingerprint(input: String): String {
        val md = MessageDigest.getInstance("MD5")
        val bytes = md.digest(input.toByteArray())
        return bytes.take(8).joinToString("") { "%02x".format(it) }
    }
}
