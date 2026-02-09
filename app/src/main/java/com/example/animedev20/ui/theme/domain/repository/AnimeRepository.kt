package com.example.animedev20.ui.theme.domain.repository

import com.example.animedev20.ui.theme.domain.model.Anime
import com.example.animedev20.ui.theme.domain.model.AnimeDetail

interface AnimeRepository {
    suspend fun getHeroRecommendation(): Anime
    suspend fun getAnimesByGenre(genreId: String): List<Anime>
    suspend fun getAnimeDetail(animeId: Long): AnimeDetail
    suspend fun searchAnime(query: String): List<Anime>
}
