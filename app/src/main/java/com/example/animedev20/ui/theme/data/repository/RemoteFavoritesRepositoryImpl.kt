package com.example.animedev20.ui.theme.data.repository

import android.util.Log
import com.example.animedev20.ui.theme.data.remote.AnimeApi
import com.example.animedev20.ui.theme.domain.model.Anime
import com.example.animedev20.ui.theme.domain.repository.FavoritesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RemoteFavoritesRepositoryImpl(
    private val animeApi: AnimeApi,
    scope: CoroutineScope
) : FavoritesRepository {

    companion object {
        private const val TAG = "RemoteFavoritesRepo"
    }

    private val favoriteAnimes = MutableStateFlow<List<Anime>>(emptyList())
    private val refreshMutex = Mutex()

    override val favorites: Flow<List<Anime>> = favoriteAnimes.asStateFlow()

    init {
        scope.launch {
            refreshFavorites()
        }
    }

    override suspend fun refreshFavorites() {
        refreshMutex.withLock {
            runCatching {
                val favoriteIds = animeApi.getMyFavoriteIds().data.distinct()

                val restoredFavorites = coroutineScope {
                    favoriteIds.map { animeId ->
                        async {
                            runCatching {
                                /**
                                 * Se usa /anime/{id}/detail porque ese endpoint entrega
                                 * la sinopsis ya traducida al español desde backend.
                                 *
                                 * Esto corrige favoritos y también la sección de trivias,
                                 * porque las trivias se alimentan desde favoritesRepository.favorites.
                                 */
                                animeApi.getDetail(animeId).data.anime
                            }.onFailure { error ->
                                Log.w(
                                    TAG,
                                    "No se pudo cargar el detalle traducido del favorito id=$animeId",
                                    error
                                )
                            }.getOrNull()
                        }
                    }.awaitAll().filterNotNull()
                }

                favoriteAnimes.value = restoredFavorites
            }.onFailure { error ->
                Log.w(TAG, "No se pudieron restaurar los favoritos remotos", error)
                // NO vaciar el estado actual en fallos transitorios
            }
        }
    }

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
        if (isFavorite(anime.id).first()) {
            removeFavorite(anime.id)
        } else {
            addFavorite(anime)
        }
    }

    override fun isFavorite(animeId: Long): Flow<Boolean> = favoriteAnimes
        .map { list -> list.any { it.id == animeId } }
        .distinctUntilChanged()
}