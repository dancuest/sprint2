package com.example.animedev20.ui.theme.data.repository

import com.example.animedev20.ui.theme.domain.model.Anime
import com.example.animedev20.ui.theme.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

object FakeFavoritesRepositoryImpl : FavoritesRepository {

    private val favoriteAnimes = MutableStateFlow<List<Anime>>(emptyList())

    override val favorites: Flow<List<Anime>> = favoriteAnimes.asStateFlow()

    override suspend fun addFavorite(anime: Anime) {
        val current = favoriteAnimes.value
        if (current.any { it.id == anime.id }) return
        favoriteAnimes.value = current + anime
    }

    override suspend fun removeFavorite(animeId: Long) {
        val current = favoriteAnimes.value
        favoriteAnimes.value = current.filterNot { it.id == animeId }
    }

    override suspend fun toggleFavorite(anime: Anime) {
        if (favoriteAnimes.value.any { it.id == anime.id }) {
            removeFavorite(anime.id)
        } else {
            addFavorite(anime)
        }
    }

    override fun isFavorite(animeId: Long): Flow<Boolean> =
    favoriteAnimes
    .map { list -> list.any { it.id == animeId } }
    .distinctUntilChanged()
}