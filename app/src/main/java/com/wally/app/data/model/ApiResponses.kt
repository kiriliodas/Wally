package com.wally.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// --- Wallhaven DTOs ---
@Serializable
data class WallhavenResponse(
    val data: List<WallhavenImage> = emptyList()
)

@Serializable
data class WallhavenImage(
    val id: String = "",
    val path: String = "",
    val views: Int = 0,
    val favorites: Int = 0,
    @SerialName("dimension_x") val dimensionX: Int = 0,
    @SerialName("dimension_y") val dimensionY: Int = 0,
    val ratio: String = "",
    val thumbs: WallhavenThumbs = WallhavenThumbs(),
    val colors: List<String> = emptyList(),
    val uploader: WallhavenUploader? = null
)

@Serializable
data class WallhavenThumbs(
    val large: String = "",
    val original: String = "",
    val small: String = ""
)

@Serializable
data class WallhavenUploader(
    val username: String = ""
)

// --- Unsplash DTOs ---
@Serializable
data class UnsplashSearchResponse(
    val results: List<UnsplashPhoto> = emptyList()
)

@Serializable
data class UnsplashPhoto(
    val id: String = "",
    val width: Int = 0,
    val height: Int = 0,
    val color: String? = null,
    val likes: Int = 0,
    val description: String? = null,
    val urls: UnsplashUrls = UnsplashUrls(),
    val user: UnsplashUser = UnsplashUser()
)

@Serializable
data class UnsplashUrls(
    val raw: String = "",
    val full: String = "",
    val regular: String = "",
    val small: String = "",
    val thumb: String = ""
)

@Serializable
data class UnsplashUser(
    val name: String = "Unsplash Contributor",
    val username: String = "",
    val links: UnsplashUserLinks? = null
)

@Serializable
data class UnsplashUserLinks(
    val html: String = ""
)

// --- Pexels DTOs ---
@Serializable
data class PexelsResponse(
    val photos: List<PexelsPhoto> = emptyList()
)

@Serializable
data class PexelsPhoto(
    val id: Long = 0L,
    val width: Int = 0,
    val height: Int = 0,
    val url: String = "",
    val photographer: String = "Pexels Artist",
    @SerialName("photographer_url") val photographerUrl: String? = null,
    @SerialName("avg_color") val avgColor: String? = null,
    val src: PexelsSrc = PexelsSrc()
)

@Serializable
data class PexelsSrc(
    val original: String = "",
    val large2x: String = "",
    val large: String = "",
    val portrait: String = ""
)

// --- Pixabay DTOs ---
@Serializable
data class PixabayResponse(
    val hits: List<PixabayHit> = emptyList()
)

@Serializable
data class PixabayHit(
    val id: Long = 0L,
    val imageWidth: Int = 0,
    val imageHeight: Int = 0,
    val views: Int = 0,
    val downloads: Int = 0,
    val likes: Int = 0,
    val user: String = "Pixabay Creator",
    val userImageURL: String? = null,
    val largeImageURL: String = "",
    val fullHDURL: String? = null,
    val imageURL: String? = null,
    val webformatURL: String = "",
    val tags: String = ""
)
