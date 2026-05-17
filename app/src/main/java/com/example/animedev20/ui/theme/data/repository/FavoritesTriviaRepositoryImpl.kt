package com.example.animedev20.ui.theme.data.repository

import android.content.Context
import androidx.core.content.edit
import com.example.animedev20.ui.theme.data.trivia.AnimeTriviaQuestionFactory
import com.example.animedev20.ui.theme.domain.model.Anime
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaDifficulty
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaQuestion
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaSummary
import com.example.animedev20.ui.theme.domain.repository.AnimeRepository
import com.example.animedev20.ui.theme.domain.repository.FavoritesRepository
import com.example.animedev20.ui.theme.domain.repository.TriviaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlin.math.max

@Suppress("UNUSED_PARAMETER")
class FavoritesTriviaRepositoryImpl(
    private val favoritesRepository: FavoritesRepository,
    animeRepository: AnimeRepository,
    private val context: Context? = null
) : TriviaRepository {

    private companion object {
        const val PREFS_NAME = "animedev_trivia_prefs"
        const val KEY_STATS = "trivia_stats"
    }

    private data class TriviaStats(
        val timesPlayed: Int = 0,
        val lastScore: Int? = null,
        val totalQuestions: Int = AnimeTriviaQuestionFactory.questionCountForDifficulty(TriviaDifficulty.EASY),
        val lastDifficulty: TriviaDifficulty? = null,
        val bestScore: Int = 0
    )

    private val statsFlow = MutableStateFlow<Map<Int, TriviaStats>>(emptyMap())

    init {
        restoreStats()
    }

    override fun getTriviaSummaries(): Flow<List<TriviaSummary>> =
        favoritesRepository.favorites.combine(statsFlow) { favorites, stats ->
            favorites
                .sortedBy { it.title.lowercase() }
                .map { anime ->
                    val animeStats = stats[anime.id.toInt()]
                    val difficulty = animeStats?.lastDifficulty ?: TriviaDifficulty.EASY

                    TriviaSummary(
                        anime = anime,
                        lastScore = animeStats?.lastScore,
                        totalQuestions = animeStats?.totalQuestions
                            ?: AnimeTriviaQuestionFactory.questionCountForDifficulty(difficulty),
                        lastDifficulty = animeStats?.lastDifficulty,
                        bestScore = animeStats?.bestScore ?: 0
                    )
                }
        }

    override suspend fun getQuestions(
        animeId: Long,
        difficulty: TriviaDifficulty
    ): List<TriviaQuestion> {
        throw IllegalStateException(
            "La app está intentando usar el repositorio local de trivias. " +
                    "Eso significa que no se está usando el backend remoto. " +
                    "Revisa ApiConfig, conexión a internet y recompila la app."
        )
    }

    override suspend fun recordResult(
        animeId: Long,
        difficulty: TriviaDifficulty,
        score: Int,
        totalQuestions: Int
    ) {
        statsFlow.update { current ->
            val key = animeId.toInt()
            val previous = current[key]

            current + (
                    key to TriviaStats(
                        timesPlayed = (previous?.timesPlayed ?: 0) + 1,
                        lastScore = score,
                        totalQuestions = totalQuestions,
                        lastDifficulty = difficulty,
                        bestScore = max(previous?.bestScore ?: 0, score)
                    )
                    )
        }

        persistStats()
    }

    private fun restoreStats() {
        val appContext = context?.applicationContext ?: return

        val rawStats = appContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_STATS, emptySet())
            .orEmpty()

        val restored = rawStats.mapNotNull { encoded ->
            val parts = encoded.split("|")

            if (parts.size != 6) {
                return@mapNotNull null
            }

            val animeId = parts[0].toIntOrNull()
                ?: return@mapNotNull null

            val timesPlayed = parts[1].toIntOrNull() ?: 0
            val lastScore = parts[2].toIntOrNull()

            val lastDifficulty = parts[4]
                .takeIf { it.isNotBlank() }
                ?.let { difficultyName ->
                    runCatching {
                        TriviaDifficulty.valueOf(difficultyName)
                    }.getOrNull()
                }

            val safeDifficulty = lastDifficulty ?: TriviaDifficulty.EASY
            val totalQuestions = parts[3].toIntOrNull()
                ?: AnimeTriviaQuestionFactory.questionCountForDifficulty(safeDifficulty)

            val bestScore = parts[5].toIntOrNull() ?: 0

            animeId to TriviaStats(
                timesPlayed = timesPlayed,
                lastScore = lastScore,
                totalQuestions = totalQuestions,
                lastDifficulty = lastDifficulty,
                bestScore = bestScore
            )
        }.toMap()

        statsFlow.value = restored
    }

    private fun persistStats() {
        val appContext = context?.applicationContext ?: return

        val encoded = statsFlow.value.map { (animeId, stats) ->
            listOf(
                animeId.toString(),
                stats.timesPlayed.toString(),
                stats.lastScore?.toString().orEmpty(),
                stats.totalQuestions.toString(),
                stats.lastDifficulty?.name.orEmpty(),
                stats.bestScore.toString()
            ).joinToString("|")
        }.toSet()

        appContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit {
                putStringSet(KEY_STATS, encoded)
            }
    }
}