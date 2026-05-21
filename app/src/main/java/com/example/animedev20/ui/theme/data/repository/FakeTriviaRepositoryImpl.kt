package com.example.animedev20.ui.theme.data.repository

import com.example.animedev20.ui.theme.data.FakeDataSource
import com.example.animedev20.ui.theme.data.trivia.AnimeTriviaQuestionFactory
import com.example.animedev20.ui.theme.domain.model.Anime
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaDifficulty
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaQuestion
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaSummary
import com.example.animedev20.ui.theme.domain.repository.TriviaRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

object FakeTriviaRepositoryImpl : TriviaRepository {

    private const val DEFAULT_QUESTION_COUNT = 3

    private data class TriviaStats(
        val timesPlayed: Int = 0,
        val lastScore: Int? = null,
        val totalQuestions: Int = DEFAULT_QUESTION_COUNT,
        val lastDifficulty: TriviaDifficulty? = null,
        val bestScore: Int = 0
    )

    private val statsFlow = MutableStateFlow<Map<Long, TriviaStats>>(emptyMap())

    override fun getTriviaSummaries(): Flow<List<TriviaSummary>> =
        statsFlow.map { stats ->
            FakeDataSource.animeCatalog
                .sortedBy { anime -> anime.title.lowercase() }
                .map { anime ->
                    val animeStats = stats[anime.id]

                    TriviaSummary(
                        anime = anime,
                        lastScore = animeStats?.lastScore,
                        totalQuestions = animeStats?.totalQuestions
                            ?: AnimeTriviaQuestionFactory.questionCountForDifficulty(
                                animeStats?.lastDifficulty ?: TriviaDifficulty.EASY
                            ),
                        lastDifficulty = animeStats?.lastDifficulty,
                        bestScore = animeStats?.bestScore ?: 0
                    )
                }
        }

    override suspend fun getQuestions(
        animeId: Long,
        difficulty: TriviaDifficulty
    ): List<TriviaQuestion> {
        delay(400)

        val anime = findAnimeById(animeId)

        val questions = AnimeTriviaQuestionFactory.build(
            anime = anime,
            difficulty = difficulty
        )

        if (questions.isEmpty()) {
            error("No hay preguntas seguras disponibles para ${anime.title}")
        }

        return questions
    }

    override suspend fun recordResult(
        animeId: Long,
        difficulty: TriviaDifficulty,
        score: Int,
        totalQuestions: Int
    ) {
        statsFlow.update { current ->
            val previous = current[animeId]

            current + (
                    animeId to TriviaStats(
                        timesPlayed = (previous?.timesPlayed ?: 0) + 1,
                        lastScore = score,
                        totalQuestions = totalQuestions,
                        lastDifficulty = difficulty,
                        bestScore = maxOf(previous?.bestScore ?: 0, score)
                    )
                    )
        }
    }

    private fun findAnimeById(animeId: Long): Anime {
        return FakeDataSource.animeCatalog.firstOrNull { anime ->
            anime.id == animeId
        } ?: error("No hay trivias disponibles para el anime con id $animeId")
    }
}