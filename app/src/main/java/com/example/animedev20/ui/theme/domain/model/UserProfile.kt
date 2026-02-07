package com.example.animedev20.ui.theme.domain.model

import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaDifficulty

data class UserProfile(
    val id: String,
    val name: String,
    val nickname: String,
    val email: String,
    val avatarUrl: String,
    val knowledgeLevel: String,
    val xpPoints: Int,
    val biography: String,
    val totalAnimesWatched: Int,
    val completedTrivias: Int,
    val preferredDurations: List<DurationType>,
    val favoriteGenres: List<Genre>,
    val badges: List<String>,
    val favoriteQuote: String? = null
)

data class UserSettings(
    val preferredGenres: List<Genre>,
    val preferredDurations: List<DurationType>,
    val notificationsEnabled: Boolean,
    val culturalAlertsEnabled: Boolean,
    val autoplayNextEpisode: Boolean,
    val hasCompletedOnboarding: Boolean
)

data class TriviaProfileStats(
    val totalAnswered: Int,
    val perfectRuns: Int,
    val masteryLevel: String,
    val scoresByDifficulty: Map<TriviaDifficulty, Int>
)
