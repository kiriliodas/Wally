package com.wally.app.data.api

import com.wally.app.data.model.WallhavenResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WallhavenApiService {
    @GET("api/v1/search")
    suspend fun searchWallpapers(
        @Query("q") query: String? = null,
        @Query("categories") categories: String = "111",
        @Query("purity") purity: String = "100",
        @Query("sorting") sorting: String = "toplist",
        @Query("order") order: String = "desc",
        @Query("atleast") atleast: String = "1440x2560",
        @Query("ratios") ratios: String = "9x16,9x18,9x19.5",
        @Query("page") page: Int = 1,
        @Query("apikey") apiKey: String? = null
    ): WallhavenResponse
}
