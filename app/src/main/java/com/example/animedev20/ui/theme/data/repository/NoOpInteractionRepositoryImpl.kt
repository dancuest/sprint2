package com.example.animedev20.ui.theme.data.repository

import com.example.animedev20.ui.theme.domain.repository.InteractionRepository

object NoOpInteractionRepositoryImpl : InteractionRepository {
    override fun trackView(animeId: Long) = Unit
    override fun trackFavorite(animeId: Long) = Unit
    override fun trackUnfavorite(animeId: Long) = Unit
    override fun trackTriviaScore(animeId: Long, score: Int, totalQuestions: Int) = Unit
}
