package com.example.animedev20.ui.theme.feature.profile.ui

import com.example.animedev20.ui.theme.data.FakeDataSource
import com.example.animedev20.ui.theme.domain.model.Anime
import com.example.animedev20.ui.theme.domain.model.TriviaProfileStats
import com.example.animedev20.ui.theme.domain.model.UserProfile

data class ProfileUiState(
    val isLoading: Boolean = true,
    val profile: UserProfile? = null,
    val recentAnimes: List<Anime> = emptyList(),
    val triviaStats: TriviaProfileStats = FakeDataSource.defaultTriviaStats,
    val errorMessage: String? = null
)
