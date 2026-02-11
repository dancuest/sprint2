package com.example.animedev20.ui.theme.data.remote

import com.example.animedev20.ui.theme.domain.model.Anime
import com.example.animedev20.ui.theme.domain.model.AnimeDetail
import com.example.animedev20.ui.theme.domain.model.Genre
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AnimeApi {
    @GET("anime/hero")
    suspend fun getHero(): ApiResponse<Anime>

    @GET("anime/top")
    suspend fun getTop(@Query("limit") limit: Int = 10): ApiResponse<List<Anime>>

    @GET("anime/by-genre/{genreId}")
    suspend fun getByGenre(
        @Path("genreId") genreId: String,
        @Query("limit") limit: Int = 10
    ): ApiResponse<List<Anime>>

    @GET("anime/search")
    suspend fun search(
        @Query("q") q: String,
        @Query("limit") limit: Int = 10
    ): ApiResponse<List<Anime>>

    @GET("anime/{id}/detail")
    suspend fun getDetail(@Path("id") id: Long): ApiResponse<AnimeDetail>

    @GET("anime/{id}")
    suspend fun getById(@Path("id") id: Long): ApiResponse<Anime>

    @GET("genres")
    suspend fun getGenres(
        @Query("includeAdult") includeAdult: Boolean = false
    ): ApiResponse<List<Genre>>
}
