package com.wally.app.data.source

import com.wally.app.data.model.WallpaperItem
import com.wally.app.data.model.WallpaperSourceType

interface WallpaperSource {
    val sourceType: WallpaperSourceType
    val priorityWeight: Int // 1 (highest) to 4 (lowest)
    val isConfigured: Boolean
    suspend fun fetchWallpapers(query: String?, category: String?, page: Int): List<WallpaperItem>
}
