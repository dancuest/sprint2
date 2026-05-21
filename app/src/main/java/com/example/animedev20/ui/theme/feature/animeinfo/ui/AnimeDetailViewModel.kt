package com.example.animedev20.ui.theme.feature.animeinfo.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.animedev20.ui.theme.domain.repository.AnimeRepository
import com.example.animedev20.ui.theme.domain.repository.FavoritesRepository
import com.example.animedev20.ui.theme.domain.repository.InteractionRepository
import com.example.animedev20.ui.theme.domain.usecase.GetAnimeDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnimeDetailViewModel(
    private val animeId: Long,
    private val getAnimeDetailUseCase: GetAnimeDetailUseCase,
    private val favoritesRepository: FavoritesRepository,
    private val interactionRepository: InteractionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AnimeDetailUiState>(AnimeDetailUiState.Loading)
    val uiState: StateFlow<AnimeDetailUiState> = _uiState.asStateFlow()

    private var latestFavoriteState: Boolean = false
    private var hasTrackedView: Boolean = false

    init {
        refreshFavoritesSnapshot()
        observeFavoriteStatus()
        loadAnimeDetail()
    }

    fun loadAnimeDetail() {
        viewModelScope.launch {
            _uiState.value = AnimeDetailUiState.Loading
            val result = getAnimeDetailUseCase(animeId)
            result.fold(
                onSuccess = { detail ->
                    _uiState.value = AnimeDetailUiState.Success(detail, latestFavoriteState)
                    if (!hasTrackedView) {
                        runCatching {
                            interactionRepository.trackView(detail.anime.id)
                        }.onSuccess {
                            hasTrackedView = true
                        }.onFailure { error ->
                            Log.w(TAG, "No se pudo registrar la vista del anime ${detail.anime.id}", error)
                        }
                    }
                },
                onFailure = { throwable ->
                    _uiState.value = AnimeDetailUiState.Error(
                        throwable.message ?: "No pudimos cargar la información del anime"
                    )
                }
            )
        }
    }

    fun toggleFavorite() {
        val currentState = _uiState.value
        if (currentState is AnimeDetailUiState.Success) {
            viewModelScope.launch {
                runCatching {
                    favoritesRepository.toggleFavorite(currentState.detail.anime)
                }.onFailure { error ->
                    Log.w(
                        TAG,
                        "No se pudo actualizar el favorito del anime ${currentState.detail.anime.id}",
                        error
                    )
                }
            }
        }
    }

    private fun refreshFavoritesSnapshot() {
        viewModelScope.launch {
            runCatching {
                favoritesRepository.refreshFavorites()
            }
        }
    }

    private fun observeFavoriteStatus() {
        viewModelScope.launch {
            favoritesRepository.isFavorite(animeId).collect { isFavorite ->
                latestFavoriteState = isFavorite
                val currentState = _uiState.value
                if (currentState is AnimeDetailUiState.Success) {
                    _uiState.value = currentState.copy(isFavorite = isFavorite)
                }
            }
        }
    }

    companion object {
        private const val TAG = "AnimeDetailViewModel"

        fun provideFactory(
            animeId: Long,
            animeRepository: AnimeRepository,
            favoritesRepository: FavoritesRepository,
            interactionRepository: InteractionRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val useCase = GetAnimeDetailUseCase(animeRepository)
                    return AnimeDetailViewModel(
                        animeId = animeId,
                        getAnimeDetailUseCase = useCase,
                        favoritesRepository = favoritesRepository,
                        interactionRepository = interactionRepository
                    ) as T
                }
            }
    }
}