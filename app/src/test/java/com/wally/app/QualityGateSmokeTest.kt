package com.wally.app

import com.wally.app.data.model.WallpaperItem
import com.wally.app.data.model.WallpaperSourceType
import com.wally.app.data.quality.QualityGate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class QualityGateSmokeTest {

    private lateinit var qualityGate: QualityGate

    @Before
    fun setUp() {
        qualityGate = QualityGate(
            minLongEdge = 2560,
            minAspectRatio = 0.40f,
            maxAspectRatio = 0.62f,
            wallhavenMinFavorites = 5,
            unsplashMinLikes = 5,
            pixabayMinLikes = 5
        )
    }

    @Test
    fun testValidHighResPortraitWallpaperPasses() {
        val validItem = WallpaperItem(
            id = "valid_01",
            source = WallpaperSourceType.WALLHAVEN,
            fullUrl = "https://example.com/full.jpg",
            previewUrl = "https://example.com/prev.jpg",
            width = 1440,
            height = 3200, // Long edge 3200 >= 2560, ratio 1440/3200 = 0.45 (in 0.40..0.62)
            likesOrFavorites = 25,
            perceptualHash = "1234567890abcdef"
        )
        assertTrue(qualityGate.passesQuality(validItem))
        assertTrue(qualityGate.checkAndRegisterUnique(validItem))
    }

    @Test
    fun testLowResolutionWallpaperRejected() {
        val lowResItem = WallpaperItem(
            id = "lowres_01",
            source = WallpaperSourceType.WALLHAVEN,
            fullUrl = "https://example.com/low.jpg",
            previewUrl = "https://example.com/low.jpg",
            width = 1080,
            height = 1920, // Long edge 1920 < 2560
            likesOrFavorites = 50,
            perceptualHash = "abcdef1234567890"
        )
        assertFalse("Low resolution wallpaper should be rejected", qualityGate.passesQuality(lowResItem))
    }

    @Test
    fun testLandscapeWallpaperRejected() {
        val landscapeItem = WallpaperItem(
            id = "landscape_01",
            source = WallpaperSourceType.WALLHAVEN,
            fullUrl = "https://example.com/land.jpg",
            previewUrl = "https://example.com/land.jpg",
            width = 3840,
            height = 2160, // Landscape ratio 3840/2160 = 1.77 > 0.62
            likesOrFavorites = 100,
            perceptualHash = "fedcba0987654321"
        )
        assertFalse("Landscape wallpaper should be rejected", qualityGate.passesQuality(landscapeItem))
    }

    @Test
    fun testLowEngagementWallpaperRejected() {
        val lowEngagementItem = WallpaperItem(
            id = "low_eng_01",
            source = WallpaperSourceType.WALLHAVEN,
            fullUrl = "https://example.com/valid_res.jpg",
            previewUrl = "https://example.com/valid_res.jpg",
            width = 1440,
            height = 2960,
            likesOrFavorites = 2, // Below Wallhaven floor (5)
            perceptualHash = "aabbccddeeff0011"
        )
        assertFalse("Low engagement item should be rejected", qualityGate.passesQuality(lowEngagementItem))
    }

    @Test
    fun testDuplicatePerceptualHashDetected() {
        val item1 = WallpaperItem(
            id = "item_1",
            source = WallpaperSourceType.WALLHAVEN,
            fullUrl = "https://example.com/1.jpg",
            previewUrl = "https://example.com/1.jpg",
            width = 1440,
            height = 3200,
            likesOrFavorites = 20,
            perceptualHash = "0000000000000000"
        )
        val item2 = WallpaperItem(
            id = "item_2",
            source = WallpaperSourceType.UNSPLASH,
            fullUrl = "https://example.com/2.jpg",
            previewUrl = "https://example.com/2.jpg",
            width = 1440,
            height = 3200,
            likesOrFavorites = 20,
            perceptualHash = "0000000000000001" // Hamming distance 1 <= 4 -> duplicate!
        )

        assertTrue(qualityGate.checkAndRegisterUnique(item1))
        assertFalse("Duplicate perceptual hash should be detected and rejected", qualityGate.checkAndRegisterUnique(item2))
    }
}
