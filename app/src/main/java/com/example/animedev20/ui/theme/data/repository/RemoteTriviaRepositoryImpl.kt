package com.example.animedev20.ui.theme.data.repository

import android.util.Log
import com.example.animedev20.ui.theme.data.remote.TriviaApi
import com.example.animedev20.ui.theme.data.remote.toDomainQuestion
import com.example.animedev20.ui.theme.data.trivia.AnimeTriviaQuestionFactory
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaDifficulty
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaQuestion
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaSummary
import com.example.animedev20.ui.theme.domain.repository.TriviaRepository
import kotlinx.coroutines.flow.Flow

class RemoteTriviaRepositoryImpl(
    private val triviaApi: TriviaApi,
    private val fallbackRepository: TriviaRepository
) : TriviaRepository {

    companion object {
        private const val TAG = "RemoteTriviaRepo"
    }

    override fun getTriviaSummaries(): Flow<List<TriviaSummary>> {
        return fallbackRepository.getTriviaSummaries()
    }

    override suspend fun getQuestions(
        animeId: Long,
        difficulty: TriviaDifficulty
    ): List<TriviaQuestion> {
        val questionLimit = AnimeTriviaQuestionFactory.questionCountForDifficulty(difficulty)

        val remoteQuestions = runCatching {
            triviaApi.getApprovedQuestions(
                animeId = animeId,
                difficulty = difficulty.name,
                limit = questionLimit
            ).data
                .mapNotNull { remoteQuestion -> remoteQuestion.toDomainQuestion() }
                .filter { question -> question.difficulty == difficulty }
                .shuffled()
                .take(questionLimit)
        }.onFailure { error ->
            Log.w(
                TAG,
                "No se pudieron cargar preguntas remotas para animeId=$animeId difficulty=$difficulty",
                error
            )
        }.getOrDefault(emptyList())

        if (remoteQuestions.isNotEmpty()) {
            return remoteQuestions
        }

        return fallbackRepository.getQuestions(
            animeId = animeId,
            difficulty = difficulty
        )
    }

    override suspend fun recordResult(
        animeId: Long,
        difficulty: TriviaDifficulty,
        score: Int,
        totalQuestions: Int
    ) {
        fallbackRepository.recordResult(
            animeId = animeId,
            difficulty = difficulty,
            score = score,
            totalQuestions = totalQuestions
        )
    }
}