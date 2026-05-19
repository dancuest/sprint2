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
        val playQuestionLimit = AnimeTriviaQuestionFactory.playQuestionCountForDifficulty(difficulty)
        val bankRequestLimit = AnimeTriviaQuestionFactory.bankTargetQuestionCountForDifficulty(difficulty)

        val remoteQuestions = runCatching {
            val response = triviaApi.getApprovedQuestions(
                animeId = animeId,
                difficulty = difficulty.name,
                limit = bankRequestLimit
            )

            response.data
                .mapNotNull { remoteQuestion -> remoteQuestion.toDomainQuestion() }
                .filter { question -> question.difficulty == difficulty }
                .shuffled()
        }.onFailure { error ->
            Log.e(
                TAG,
                "Error cargando preguntas remotas animeId=$animeId difficulty=$difficulty",
                error
            )
        }.getOrElse { error ->
            throw IllegalStateException(
                "No fue posible conectar con el banco de preguntas de AnimeDev. " +
                        "Verifica internet, backend en Render y que la app esté usando la URL remota.",
                error
            )
        }

        if (remoteQuestions.isEmpty()) {
            throw IllegalStateException(
                "Este anime aún no tiene preguntas reales aprobadas para ${difficulty.name}. " +
                        "Carga preguntas desde el panel de administración antes de jugar esta dificultad."
            )
        }

        if (remoteQuestions.size < playQuestionLimit) {
            throw IllegalStateException(
                "Este anime tiene ${remoteQuestions.size} preguntas reales para ${difficulty.name}, " +
                        "pero esta dificultad necesita mínimo $playQuestionLimit para jugar. " +
                        "Carga las preguntas faltantes desde el panel de administración."
            )
        }

        return remoteQuestions.take(playQuestionLimit)
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