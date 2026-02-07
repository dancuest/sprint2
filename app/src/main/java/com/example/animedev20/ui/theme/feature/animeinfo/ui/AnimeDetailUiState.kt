package com.example.animedev20.ui.theme.feature.animeinfo.ui

import com.example.animedev20.ui.theme.domain.model.AnimeDetail

sealed class AnimeDetailUiState {
    object Loading : AnimeDetailUiState()
    data class Success(val detail: AnimeDetail, val isFavorite: Boolean) : AnimeDetailUiState()
    data class Error(val message: String) : AnimeDetailUiState()
}