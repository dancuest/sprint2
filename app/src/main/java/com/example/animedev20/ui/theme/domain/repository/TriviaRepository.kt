package com.example.animedev20.ui.theme.domain.repository

import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaDifficulty
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaQuestion
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaSummary
import kotlinx.coroutines.flow.Flow

interface TriviaRepository {
    fun getTriviaSummaries(): Flow<List<TriviaSummary>>
    suspend fun getQuestions(animeId: Long, difficulty: TriviaDifficulty): List<TriviaQuestion>
    suspend fun recordResult(animeId: Long, difficulty: TriviaDifficulty, score: Int, totalQuestions: Int)
}