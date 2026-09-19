package com.wally.app.data.api

import com.wally.app.data.model.PexelsResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface PexelsApiService {
    @GET("v1/search")
    suspend fun searchPhotos(
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
        @Query("orientation") orientation: String = "portrait",
        @Query("size") size: String = "large",
        @Header("Authorization") apiKey: String
    ): PexelsResponse
}
