package com.example.animedev20.ui.theme.data.repository

import com.example.animedev20.ui.theme.domain.model.Anime
import com.example.animedev20.ui.theme.domain.repository.FavoritesRepository
import com.example.animedev20.ui.theme.domain.repository.InteractionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class TrackingFavoritesRepositoryImpl(
    private val delegate: FavoritesRepository,
    private val interactionRepository: InteractionRepository
) : FavoritesRepository {
    override val favorites: Flow<List<Anime>> = delegate.favorites

    override suspend fun addFavorite(anime: Anime) {
        delegate.addFavorite(anime)
        interactionRepository.trackFavorite(anime.id)
    }

    override suspend fun removeFavorite(animeId: Long) {
        delegate.removeFavorite(animeId)
        interactionRepository.trackUnfavorite(animeId)
    }

    override suspend fun toggleFavorite(anime: Anime) {
        val wasFavorite = delegate.isFavorite(anime.id).first()
        delegate.toggleFavorite(anime)
        if (wasFavorite) {
            interactionRepository.trackUnfavorite(anime.id)
        } else {
            interactionRepository.trackFavorite(anime.id)
        }
    }

    override fun isFavorite(animeId: Long): Flow<Boolean> = delegate.isFavorite(animeId)
}
