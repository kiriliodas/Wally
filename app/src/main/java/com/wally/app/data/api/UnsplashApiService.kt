package com.wally.app.data.api

import com.wally.app.data.model.UnsplashSearchResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface UnsplashApiService {
    @GET("search/photos")
    suspend fun searchPhotos(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
        @Query("orientation") orientation: String = "portrait",
        @Header("Authorization") authorization: String
    ): UnsplashSearchResponse
}
