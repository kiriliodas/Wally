package com.wally.app.data

/** The provider is kept on each item so the merge order can be inspected and debugged. */
enum class WallpaperOrigin(val label: String) {
    WALLHAVEN("Wallhaven"),
    UNSPLASH("Unsplash"),
    PEXELS("Pexels"),
    PIXABAY("Pixabay"),
    CURATED("Wally curated"),
    UPLOAD("Your image"),
}

data class WallpaperItem(
    val id: String,
    val title: String,
    val imageUrl: String,
    val width: Int,
    val height: Int,
    val origin: WallpaperOrigin,
    val qualitySignal: Int? = null,
    val tags: List<String> = emptyList(),
) {
    val stableId: String
        get() = "${origin.name.lowercase()}:$id"

    val aspectRatio: Float
        get() = width.toFloat() / height.coerceAtLeast(1).toFloat()

    val isPortrait: Boolean
        get() = height >= width
}

object QualityGate {
    private const val MIN_LONG_EDGE = 2560
    private const val MIN_PIXELS = 1440L * 2560L
    private const val MIN_RATIO = 9f / 20.5f
    private const val MAX_RATIO = 9f / 16f

    /**
     * One gate is applied after every provider mapper and before anything reaches Compose.
     * A source-specific engagement floor is used only where that provider exposes a signal.
     */
    fun accepts(item: WallpaperItem): Boolean {
        if (item.origin == WallpaperOrigin.UPLOAD) return true
        if (item.width <= 0 || item.height <= 0) return false
        if (maxOf(item.width, item.height) < MIN_LONG_EDGE) return false
        if (item.width.toLong() * item.height.toLong() < MIN_PIXELS) return false
        if (item.aspectRatio !in MIN_RATIO..MAX_RATIO) return false

        val minimumSignal = when (item.origin) {
            WallpaperOrigin.WALLHAVEN -> 4
            WallpaperOrigin.UNSPLASH -> 12
            WallpaperOrigin.PIXABAY -> 5
            WallpaperOrigin.PEXELS, WallpaperOrigin.CURATED, WallpaperOrigin.UPLOAD -> null
        }
        return minimumSignal == null || item.qualitySignal == null || item.qualitySignal >= minimumSignal
    }
}

/** A compact, on-demand fallback keeps the first screen useful when API keys are absent/offline. */
object FallbackCatalog {
    private fun unsplash(id: String): String =
        "https://images.unsplash.com/$id?auto=format&fit=crop&w=1440&h=2560&q=84"

    val items: List<WallpaperItem> = listOf(
        WallpaperItem("curated-01", "Quiet Coast", unsplash("photo-1500534623283-312aade485b7"), 1440, 2560, WallpaperOrigin.CURATED, 100, listOf("Nature", "Minimal")),
        WallpaperItem("curated-02", "Night Pines", unsplash("photo-1511497584788-876760111969"), 1440, 2560, WallpaperOrigin.CURATED, 100, listOf("Nature", "Dark")),
        WallpaperItem("curated-03", "Amber Dunes", unsplash("photo-1500530855697-b586d89ba3ee"), 1440, 2560, WallpaperOrigin.CURATED, 100, listOf("Nature", "Abstract")),
        WallpaperItem("curated-04", "Moss Study", unsplash("photo-1473448912268-2022ce9509d8"), 1440, 2560, WallpaperOrigin.CURATED, 100, listOf("Nature", "Minimal")),
        WallpaperItem("curated-05", "Stone & Sky", unsplash("photo-1519681393784-d120267933ba"), 1440, 2560, WallpaperOrigin.CURATED, 100, listOf("Nature", "Dark")),
        WallpaperItem("curated-06", "Soft Geometry", unsplash("photo-1497366754035-f200968a6e72"), 1440, 2560, WallpaperOrigin.CURATED, 100, listOf("Architecture", "Minimal")),
        WallpaperItem("curated-07", "Blue Hour", unsplash("photo-1530789253388-582c481c54b0"), 1440, 2560, WallpaperOrigin.CURATED, 100, listOf("Nature", "Dark")),
        WallpaperItem("curated-08", "Desert Silence", unsplash("photo-1500534314209-a25ddb2bd429"), 1440, 2560, WallpaperOrigin.CURATED, 100, listOf("Nature", "Abstract")),
        WallpaperItem("curated-09", "Fogline", unsplash("photo-1513836279014-a89f7a76ae86"), 1440, 2560, WallpaperOrigin.CURATED, 100, listOf("Nature", "Dark")),
        WallpaperItem("curated-10", "Greenhouse", unsplash("photo-1466692476868-aef1dfb1e735"), 1440, 2560, WallpaperOrigin.CURATED, 100, listOf("Nature", "Minimal")),
        WallpaperItem("curated-11", "After Rain", unsplash("photo-1519817650390-64a93db51149"), 1440, 2560, WallpaperOrigin.CURATED, 100, listOf("Abstract", "Dark")),
        WallpaperItem("curated-12", "Leaf Light", unsplash("photo-1497250681960-ef046c08a56e"), 1440, 2560, WallpaperOrigin.CURATED, 100, listOf("Nature", "Minimal")),
    )
}
