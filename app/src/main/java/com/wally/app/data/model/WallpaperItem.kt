package com.wally.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class WallpaperItem(
    val id: String,
    val source: WallpaperSourceType,
    val fullUrl: String,
    val previewUrl: String,
    val width: Int,
    val height: Int,
    val author: String = "Unknown",
    val authorUrl: String? = null,
    val likesOrFavorites: Int = 0,
    val views: Int? = null,
    val tags: List<String> = emptyList(),
    val perceptualHash: String = "",
    val colorHex: String? = null
) {
    val aspectRatio: Float
        get() = if (height > 0) width.toFloat() / height.toFloat() else 0.5625f

    val longEdge: Int
        get() = maxOf(width, height)

    val shortEdge: Int
        get() = minOf(width, height)

    val resolutionFormatted: String
        get() = "${width} × ${height}"
}
