package com.example.animedev20.ui.theme.domain.model.Trivias

import com.example.animedev20.ui.theme.domain.model.Anime

data class TriviaSummary(
                         val anime: Anime,
                         val lastScore: Int?,
                         val totalQuestions: Int,
                         val lastDifficulty: TriviaDifficulty?,
                         val bestScore: Int
)