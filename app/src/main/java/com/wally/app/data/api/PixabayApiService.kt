package com.wally.app.data.api

import com.wally.app.data.model.PixabayResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PixabayApiService {
    @GET("api/")
    suspend fun searchPhotos(
        @Query("key") apiKey: String,
        @Query("q") query: String? = null,
        @Query("image_type") imageType: String = "photo",
        @Query("orientation") orientation: String = "vertical",
        @Query("min_width") minWidth: Int = 1440,
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20
    ): PixabayResponse
}
