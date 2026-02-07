package com.example.animedev20.ui.theme.feature.animeinfo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.animedev20.ui.theme.data.repository.FakeAnimeRepositoryImpl
import com.example.animedev20.ui.theme.data.repository.FakeFavoritesRepositoryImpl
import com.example.animedev20.ui.theme.domain.repository.FavoritesRepository
import com.example.animedev20.ui.theme.domain.usecase.GetAnimeDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AnimeDetailViewModel(
    private val animeId: Long,
    private val getAnimeDetailUseCase: GetAnimeDetailUseCase,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AnimeDetailUiState>(AnimeDetailUiState.Loading)
    val uiState: StateFlow<AnimeDetailUiState> = _uiState.asStateFlow()

    private var latestFavoriteState: Boolean = false

    init {
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
                favoritesRepository.toggleFavorite(currentState.detail.anime)
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
        fun provideFactory(animeId: Long): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repository = FakeAnimeRepositoryImpl()
                    val useCase = GetAnimeDetailUseCase(repository)
                    return AnimeDetailViewModel(
                        animeId = animeId,
                        getAnimeDetailUseCase = useCase,
                        favoritesRepository = FakeFavoritesRepositoryImpl
                    ) as T
                }
            }
    }
}