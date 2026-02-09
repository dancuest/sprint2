package com.example.animedev20.ui.theme.data.remote

import com.example.animedev20.ui.theme.domain.model.Anime
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AnimeApi {
    @GET("anime/top")
    suspend fun getTop(@Query("limit") limit: Int = 10): ApiResponse<List<Anime>>

    @GET("anime/search")
    suspend fun search(
        @Query("q") q: String,
        @Query("limit") limit: Int = 10
    ): ApiResponse<List<Anime>>

    @GET("anime/{id}")
    suspend fun getById(@Path("id") id: Long): ApiResponse<Anime>
}
