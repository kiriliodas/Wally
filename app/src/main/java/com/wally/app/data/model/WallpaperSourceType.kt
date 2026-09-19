package com.wally.app.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class WallpaperSourceType(val displayName: String, val badgeColor: Long) {
    WALLHAVEN("Wallhaven", 0xFFC8832A),
    UNSPLASH("Unsplash", 0xFFE0A04A),
    PEXELS("Pexels", 0xFF2A9D8F),
    PIXABAY("Pixabay", 0xFFE76F51)
}
