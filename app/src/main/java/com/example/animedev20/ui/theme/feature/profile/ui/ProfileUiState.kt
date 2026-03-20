package com.example.animedev20.ui.theme.feature.profile.ui

import com.example.animedev20.ui.theme.domain.model.Anime
import com.example.animedev20.ui.theme.domain.model.UserProfile

data class ProfileUiState(
    val isLoading: Boolean = true,
    val profile: UserProfile? = null,
    val favoriteAnimes: List<Anime> = emptyList(),
    val fanLevel: String = "",
    val triviaPlayedCount: Int = 0,
    val errorMessage: String? = null
)