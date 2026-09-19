package com.wally.app.data.repository

import com.wally.app.data.model.WallpaperItem
import com.wally.app.data.model.WallpaperSourceType
import com.wally.app.data.quality.QualityGate
import com.wally.app.data.source.WallpaperSource
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class WallpaperRepository(
    private val sources: List<WallpaperSource>,
    private val qualityGate: QualityGate
) {

    // Sorted by priorityWeight (Wallhaven = 1, Unsplash = 2, Pexels = 3, Pixabay = 4)
    private val sortedSources = sources.sortedBy { it.priorityWeight }

    suspend fun getWallpapers(
        query: String? = null,
        category: String? = null,
        page: Int = 1
    ): List<WallpaperItem> = coroutineScope {
        val configuredSources = sortedSources.filter { it.isConfigured }

        if (configuredSources.isEmpty()) {
            return@coroutineScope getCuratedFallbackWallpapers(category, query)
        }

        // Concurrently query all configured sources
        val deferredResults = configuredSources.map { source ->
            async {
                try {
                    source.fetchWallpapers(query, category, page)
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }

        val allResults = mutableListOf<WallpaperItem>()
        for (deferred in deferredResults) {
            val list = deferred.await()
            for (item in list) {
                if (qualityGate.passesQuality(item) && qualityGate.checkAndRegisterUnique(item)) {
                    allResults.add(item)
                }
            }
        }

        // If network results are sparse or offline, supplement with curated presets
        if (allResults.isEmpty() && page == 1) {
            val curated = getCuratedFallbackWallpapers(category, query)
            for (item in curated) {
                if (qualityGate.passesQuality(item) && qualityGate.checkAndRegisterUnique(item)) {
                    allResults.add(item)
                }
            }
        }

        allResults
    }

    fun getTodayPick(): WallpaperItem {
        return WallpaperItem(
            id = "wally_hero_pick",
            source = WallpaperSourceType.WALLHAVEN,
            fullUrl = "https://w.wallhaven.cc/full/4x/wallhaven-4x361z.jpg",
            previewUrl = "https://th.wallhaven.cc/small/4x/4x361z.jpg",
            width = 1440,
            height = 3200,
            author = "Kurogane",
            likesOrFavorites = 482,
            views = 12450,
            tags = listOf("Minimal", "Obsidian", "OLED"),
            perceptualHash = "f0a1b2c3d4e5f607",
            colorHex = "#C8832A"
        )
    }

    private fun getCuratedFallbackWallpapers(category: String?, query: String?): List<WallpaperItem> {
        val all = listOf(
            WallpaperItem(
                id = "curated_01",
                source = WallpaperSourceType.WALLHAVEN,
                fullUrl = "https://w.wallhaven.cc/full/28/wallhaven-281zom.jpg",
                previewUrl = "https://th.wallhaven.cc/small/28/281zom.jpg",
                width = 1440,
                height = 2960,
                author = "NocturnalArt",
                likesOrFavorites = 312,
                tags = listOf("Minimal", "Dark", "Geometry"),
                perceptualHash = "a1b2c3d4e5f60001",
                colorHex = "#1A1A1A"
            ),
            WallpaperItem(
                id = "curated_02",
                source = WallpaperSourceType.UNSPLASH,
                fullUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23",
                previewUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=720",
                width = 1440,
                height = 3120,
                author = "Alexander Ant",
                likesOrFavorites = 890,
                tags = listOf("Amoled", "Abstract", "Liquid"),
                perceptualHash = "a1b2c3d4e5f60002",
                colorHex = "#C8832A"
            ),
            WallpaperItem(
                id = "curated_03",
                source = WallpaperSourceType.WALLHAVEN,
                fullUrl = "https://w.wallhaven.cc/full/72/wallhaven-72g139.jpg",
                previewUrl = "https://th.wallhaven.cc/small/72/72g139.jpg",
                width = 1440,
                height = 2880,
                author = "ApexVoid",
                likesOrFavorites = 450,
                tags = listOf("Space", "Cosmic", "Dark"),
                perceptualHash = "a1b2c3d4e5f60003",
                colorHex = "#0A0A0A"
            ),
            WallpaperItem(
                id = "curated_04",
                source = WallpaperSourceType.PEXELS,
                fullUrl = "https://images.pexels.com/photos/1624496/pexels-photo-1624496.jpeg",
                previewUrl = "https://images.pexels.com/photos/1624496/pexels-photo-1624496.jpeg?auto=compress&cs=tinysrgb&dpr=2&h=650&w=940",
                width = 1440,
                height = 2560,
                author = "Eberhard Grossgasteiger",
                likesOrFavorites = 120,
                tags = listOf("Nature", "Mountain", "Night"),
                perceptualHash = "a1b2c3d4e5f60004",
                colorHex = "#2A2622"
            ),
            WallpaperItem(
                id = "curated_05",
                source = WallpaperSourceType.WALLHAVEN,
                fullUrl = "https://w.wallhaven.cc/full/y8/wallhaven-y8k2zk.jpg",
                previewUrl = "https://th.wallhaven.cc/small/y8/y8k2zk.jpg",
                width = 1440,
                height = 3088,
                author = "NeonShade",
                likesOrFavorites = 295,
                tags = listOf("Cyberpunk", "Tokyo", "Rain"),
                perceptualHash = "a1b2c3d4e5f60005",
                colorHex = "#E0A04A"
            ),
            WallpaperItem(
                id = "curated_06",
                source = WallpaperSourceType.PIXABAY,
                fullUrl = "https://pixabay.com/get/g89b21f37c35.jpg",
                previewUrl = "https://cdn.pixabay.com/photo/2020/03/19/21/25/architecture-4948834_640.jpg",
                width = 1440,
                height = 2800,
                author = "StudioK",
                likesOrFavorites = 85,
                tags = listOf("Architecture", "Minimal", "Monochrome"),
                perceptualHash = "a1b2c3d4e5f60006",
                colorHex = "#1A1A1A"
            )
        )

        if (category != null && category != "All") {
            val filtered = all.filter { it.tags.any { tag -> tag.contains(category, ignoreCase = true) } }
            if (filtered.isNotEmpty()) return filtered
        }

        if (query != null && query.isNotBlank()) {
            val filtered = all.filter {
                it.tags.any { tag -> tag.contains(query, ignoreCase = true) } ||
                it.author.contains(query, ignoreCase = true)
            }
            if (filtered.isNotEmpty()) return filtered
        }

        return all
    }
}
