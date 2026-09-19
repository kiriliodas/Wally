package com.wally.app.data.quality

import com.wally.app.data.model.WallpaperItem
import com.wally.app.data.model.WallpaperSourceType
import java.util.Collections

class QualityGate(
    private val minLongEdge: Int = 2560,
    private val minAspectRatio: Float = 0.40f,
    private val maxAspectRatio: Float = 0.62f,
    private val wallhavenMinFavorites: Int = 5,
    private val unsplashMinLikes: Int = 5,
    private val pixabayMinLikes: Int = 5
) {
    // Thread-safe set of seen hashes to deduplicate feed
    private val seenHashes = Collections.synchronizedSet(LinkedHashSet<String>())
    private val seenSourceIds = Collections.synchronizedSet(LinkedHashSet<String>())

    fun passesQuality(item: WallpaperItem): Boolean {
        // 1. Long-edge resolution check
        if (item.longEdge < minLongEdge) {
            return false
        }

        // 2. Aspect ratio check (portrait phone screen 9:16 to 9:20.5)
        val ratio = item.aspectRatio
        if (ratio !in minAspectRatio..maxAspectRatio) {
            return false
        }

        // 3. Engagement / Quality floor per source
        when (item.source) {
            WallpaperSourceType.WALLHAVEN -> {
                if (item.likesOrFavorites < wallhavenMinFavorites) return false
            }
            WallpaperSourceType.UNSPLASH -> {
                if (item.likesOrFavorites < unsplashMinLikes) return false
            }
            WallpaperSourceType.PEXELS -> {
                // Pexels curated portrait filter guarantees baseline quality
                if (item.shortEdge < 1440) return false
            }
            WallpaperSourceType.PIXABAY -> {
                if (item.likesOrFavorites < pixabayMinLikes) return false
                val totalPixels = item.width.toLong() * item.height.toLong()
                if (totalPixels < 1440L * 2560L) return false
            }
        }

        return true
    }

    /**
     * Checks if item is unique (not seen before in source ID or perceptual hash).
     * If unique, registers it and returns true.
     */
    fun checkAndRegisterUnique(item: WallpaperItem): Boolean {
        val compositeId = "${item.source.name}_${item.id}"
        if (seenSourceIds.contains(compositeId)) {
            return false
        }

        if (item.perceptualHash.isNotBlank()) {
            for (existingHash in seenHashes) {
                if (hammingDistance(item.perceptualHash, existingHash) <= 4) {
                    return false
                }
            }
            seenHashes.add(item.perceptualHash)
        }

        seenSourceIds.add(compositeId)
        return true
    }

    fun clearRegistry() {
        seenHashes.clear()
        seenSourceIds.clear()
    }

    companion object {
        fun hammingDistance(hash1: String, hash2: String): Int {
            if (hash1.length != hash2.length) return Int.MAX_VALUE
            var distance = 0
            for (i in hash1.indices) {
                val hex1 = hash1[i].digitToIntOrNull(16) ?: 0
                val hex2 = hash2[i].digitToIntOrNull(16) ?: 0
                distance += Integer.bitCount(hex1 xor hex2)
            }
            return distance
        }
    }
}
