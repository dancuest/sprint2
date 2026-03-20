package com.example.animedev20.ui.theme.domain.repository

interface InteractionRepository {
    fun trackView(animeId: Long)
    fun trackFavorite(animeId: Long)
    fun trackUnfavorite(animeId: Long)
    fun trackTriviaScore(animeId: Long, score: Int, totalQuestions: Int)
}
