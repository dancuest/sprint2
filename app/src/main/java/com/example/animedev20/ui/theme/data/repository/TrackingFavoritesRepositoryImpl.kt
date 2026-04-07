package com.example.animedev20.ui.theme.data.repository

import com.example.animedev20.ui.theme.data.refresh.HomeRefreshBus
import com.example.animedev20.ui.theme.domain.model.Anime
import com.example.animedev20.ui.theme.domain.repository.FavoritesRepository
import com.example.animedev20.ui.theme.domain.repository.InteractionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class TrackingFavoritesRepositoryImpl(
    private val delegate: FavoritesRepository,
    private val interactionRepository: InteractionRepository,
    private val homeRefreshBus: HomeRefreshBus
) : FavoritesRepository {

    override val favorites: Flow<List<Anime>> = delegate.favorites

    override suspend fun refreshFavorites() {
        delegate.refreshFavorites()
    }

    override suspend fun addFavorite(anime: Anime) {
        val alreadyFavorite = delegate.isFavorite(anime.id).first()
        if (alreadyFavorite) return

        interactionRepository.trackFavorite(anime.id)
        delegate.addFavorite(anime)

        runCatching {
            delegate.refreshFavorites()
        }

        homeRefreshBus.trigger()
    }

    override suspend fun removeFavorite(animeId: Long) {
        val wasFavorite = delegate.isFavorite(animeId).first()
        if (!wasFavorite) return

        interactionRepository.trackUnfavorite(animeId)
        delegate.removeFavorite(animeId)

        runCatching {
            delegate.refreshFavorites()
        }

        homeRefreshBus.trigger()
    }

    override suspend fun toggleFavorite(anime: Anime) {
        val wasFavorite = delegate.isFavorite(anime.id).first()

        if (wasFavorite) {
            interactionRepository.trackUnfavorite(anime.id)
            delegate.removeFavorite(anime.id)
        } else {
            interactionRepository.trackFavorite(anime.id)
            delegate.addFavorite(anime)
        }

        runCatching {
            delegate.refreshFavorites()
        }

        homeRefreshBus.trigger()
    }

    override fun isFavorite(animeId: Long): Flow<Boolean> = delegate.isFavorite(animeId)
}