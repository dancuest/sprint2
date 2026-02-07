package com.example.animedev20.ui.theme.domain.repository

import com.example.animedev20.ui.theme.domain.model.Anime
import kotlinx.coroutines.flow.Flow

interface FavoritesRepository {
    val favorites: Flow<List<Anime>>
    suspend fun addFavorite(anime: Anime)
    suspend fun removeFavorite(animeId: Long)
    suspend fun toggleFavorite(anime: Anime)
    fun isFavorite(animeId: Long): Flow<Boolean>
}