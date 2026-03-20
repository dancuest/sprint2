package com.example.animedev20.ui.theme.data.repository

import com.example.animedev20.ui.theme.data.refresh.HomeRefreshBus
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaDifficulty
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaQuestion
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaSummary
import com.example.animedev20.ui.theme.domain.repository.InteractionRepository
import com.example.animedev20.ui.theme.domain.repository.TriviaRepository
import kotlinx.coroutines.flow.Flow

class TrackingTriviaRepositoryImpl(
    private val delegate: TriviaRepository,
    private val interactionRepository: InteractionRepository,
    private val homeRefreshBus: HomeRefreshBus
) : TriviaRepository {
    override fun getTriviaSummaries(): Flow<List<TriviaSummary>> = delegate.getTriviaSummaries()

    override suspend fun getQuestions(animeId: Long, difficulty: TriviaDifficulty): List<TriviaQuestion> {
        return delegate.getQuestions(animeId, difficulty)
    }

    override suspend fun recordResult(animeId: Long, difficulty: TriviaDifficulty, score: Int, totalQuestions: Int) {
        delegate.recordResult(animeId, difficulty, score, totalQuestions)
        interactionRepository.trackTriviaScore(animeId, score, totalQuestions)
        homeRefreshBus.trigger()
    }
}
