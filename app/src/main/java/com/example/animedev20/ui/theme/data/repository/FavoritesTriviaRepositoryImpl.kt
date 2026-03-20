package com.example.animedev20.ui.theme.data.repository

import android.content.Context
import androidx.core.content.edit
import com.example.animedev20.ui.theme.data.FakeDataSource
import com.example.animedev20.ui.theme.domain.model.Anime
import com.example.animedev20.ui.theme.domain.model.DurationType
import com.example.animedev20.ui.theme.domain.model.EmissionStatus
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaDifficulty
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaQuestion
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaSummary
import com.example.animedev20.ui.theme.domain.repository.AnimeRepository
import com.example.animedev20.ui.theme.domain.repository.FavoritesRepository
import com.example.animedev20.ui.theme.domain.repository.TriviaRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlin.math.ceil
import kotlin.math.max

class FavoritesTriviaRepositoryImpl(
    private val favoritesRepository: FavoritesRepository,
    private val animeRepository: AnimeRepository,
    private val context: Context? = null
) : TriviaRepository {

    private companion object {
        const val DEFAULT_QUESTION_COUNT = 3
        const val EASY_QUESTION_COUNT = 3
        const val MEDIUM_QUESTION_COUNT = 5
        const val HARD_QUESTION_COUNT = 8
        const val PREFS_NAME = "animedev_trivia_prefs"
        const val KEY_STATS = "trivia_stats"
    }

    private data class TriviaStats(
        val timesPlayed: Int = 0,
        val lastScore: Int? = null,
        val totalQuestions: Int = DEFAULT_QUESTION_COUNT,
        val lastDifficulty: TriviaDifficulty? = null,
        val bestScore: Int = 0
    )

    private data class CulturalFoodPreference(
        val character: String,
        val itemName: String,
        val typeLabel: String,
        val context: String
    )

    private data class CulturalTraditionMoment(
        val context: String,
        val correctConcept: String,
        val distractors: List<String>,
        val detail: String
    )

    private val statsFlow = MutableStateFlow<Map<Int, TriviaStats>>(emptyMap())
    private var latestFavorites: List<Anime> = emptyList()

    init {
        restoreStats()
    }

    private val foodTypeOptions = listOf(
        "Comida de mar",
        "Dulces",
        "Comida salada",
        "Bebidas"
    )

    private val culturalFoodPreferences: Map<Long, CulturalFoodPreference> = mapOf(
        1L to CulturalFoodPreference(
            "Tanjiro",
            "mitarashi dango",
            "Dulces",
            "lo comparte con Nezuko en las calles del mercado"
        ),
        2L to CulturalFoodPreference(
            "Thorfinn",
            "onigiri relleno",
            "Comida salada",
            "recuerda los bocadillos que probó junto a mercaderes japoneses"
        ),
        3L to CulturalFoodPreference(
            "Riko",
            "dorayaki",
            "Dulces",
            "lo prepara como merienda antes de descender al Abismo"
        ),
        4L to CulturalFoodPreference(
            "el profesor Gojo",
            "taiyaki",
            "Dulces",
            "no puede resistirse a comprarlos entre misiones"
        ),
        5L to CulturalFoodPreference(
            "el Dr. Tenma",
            "té matcha",
            "Bebidas",
            "lo utiliza para recordar sus raíces japonesas en medio de Europa"
        ),
        6L to CulturalFoodPreference(
            "Winry",
            "nikuman al vapor",
            "Comida salada",
            "los comparte con los hermanos Elric tras las reparaciones"
        )
    )

    private val defaultFoodPreference = CulturalFoodPreference(
        character = "el protagonista",
        itemName = "dango",
        typeLabel = "Dulces",
        context = "como una forma de celebrar cada misión"
    )

    private val culturalTraditionMoments: Map<Long, CulturalTraditionMoment> = mapOf(
        1L to CulturalTraditionMoment(
            context = "cuando Tanjiro recuerda la Danza del Dios del Fuego",
            correctConcept = "una danza kagura dedicada a los kami",
            distractors = listOf(
                "un matsuri de verano",
                "una ceremonia del té",
                "una ofrenda de hanami"
            ),
            detail = "El Kagura del Dios del Fuego es una danza ritual que honra a los espíritus"
        ),
        2L to CulturalTraditionMoment(
            context = "cuando los guerreros comparten historias alrededor del fuego",
            correctConcept = "un cuento yorishiro para invocar protección",
            distractors = listOf(
                "una práctica de sumo",
                "un desfile de Tanabata",
                "un entrenamiento de kendo"
            ),
            detail = "Los yorishiro son objetos o narrativas que canalizan la presencia espiritual"
        ),
        3L to CulturalTraditionMoment(
            context = "cuando Riko y Reg celebran su avance en Orth",
            correctConcept = "un matsuri local para agradecer la abundancia",
            distractors = listOf(
                "una ceremonia nupcial",
                "una reunión hanami",
                "una subasta de mercado negro"
            ),
            detail = "Los matsuri se celebran para pedir protección y prosperidad a los dioses locales"
        ),
        4L to CulturalTraditionMoment(
            context = "cuando los estudiantes visitan Kyoto para el torneo escolar",
            correctConcept = "una ofrenda en un santuario sintoísta",
            distractors = listOf(
                "una iniciación ninja",
                "una procesión budista",
                "un festival de nieve"
            ),
            detail = "El arco de Kyoto muestra las plegarias en templos y ofrendas omikuji por la buena suerte"
        ),
        5L to CulturalTraditionMoment(
            context = "cuando Tenma recuerda las reuniones familiares en Japón",
            correctConcept = "una ceremonia del té para honrar a los invitados",
            distractors = listOf(
                "un ritual de kagura",
                "un festival Nebuta",
                "un acto de teatro kabuki"
            ),
            detail = "La ceremonia del té enfatiza la armonía, el respeto y la calma que Tenma añora"
        ),
        6L to CulturalTraditionMoment(
            context = "cuando los hermanos Elric observan los talismanes de Ishval",
            correctConcept = "un omamori utilizado como amuleto de protección",
            distractors = listOf(
                "un adorno de bonsái",
                "un pergamino emakimono",
                "un instrumento shamisen"
            ),
            detail = "Los omamori se consiguen en templos y se usan para desear seguridad en los viajes"
        )
    )

    private val defaultTradition = CulturalTraditionMoment(
        context = "cuando los héroes hacen una pausa para agradecer",
        correctConcept = "un ritual sintoísta para pedir protección",
        distractors = listOf(
            "una clase de caligrafía",
            "una demostración de karate",
            "un concurso gastronómico"
        ),
        detail = "Muchos animes muestran escenas donde los personajes siguen costumbres sintoístas cotidianas"
    )

    override fun getTriviaSummaries(): Flow<List<TriviaSummary>> =
        favoritesRepository.favorites.combine(statsFlow) { favorites, stats ->
            latestFavorites = favorites
            favorites
                .sortedBy { it.title.lowercase() }
                .map { anime ->
                    val animeStats = stats[anime.id.toInt()]
                    TriviaSummary(
                        anime = anime,
                        lastScore = animeStats?.lastScore,
                        totalQuestions = animeStats?.totalQuestions
                            ?: questionCountForDifficulty(animeStats?.lastDifficulty ?: TriviaDifficulty.EASY),
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

        val anime = latestFavorites.firstOrNull { it.id == animeId }
            ?: runCatching { animeRepository.getAnimeDetail(animeId).anime }.getOrNull()
            ?: throw IllegalArgumentException("Anime not found in favorites")

        if (latestFavorites.none { it.id == animeId }) {
            throw IllegalArgumentException("Anime not found in favorites")
        }

        val questions = buildQuestionSet(anime)[difficulty]
            ?: error("No hay preguntas para la dificultad $difficulty")

        return questions
            .shuffled()
            .take(questionCountForDifficulty(difficulty))
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
            current + (key to TriviaStats(
                timesPlayed = (previous?.timesPlayed ?: 0) + 1,
                lastScore = score,
                totalQuestions = totalQuestions,
                lastDifficulty = difficulty,
                bestScore = max(previous?.bestScore ?: 0, score)
            ))
        }
        persistStats()
    }

    private fun restoreStats() {
        val appContext = context?.applicationContext ?: return
        val rawStats = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getStringSet(KEY_STATS, emptySet())
            .orEmpty()

        val restored = rawStats.mapNotNull { encoded ->
            val parts = encoded.split("|")
            if (parts.size != 6) return@mapNotNull null

            val animeId = parts[0].toIntOrNull() ?: return@mapNotNull null
            val timesPlayed = parts[1].toIntOrNull() ?: 0
            val lastScore = parts[2].toIntOrNull()
            val totalQuestions = parts[3].toIntOrNull() ?: DEFAULT_QUESTION_COUNT
            val lastDifficulty = parts[4]
                .takeIf { it.isNotBlank() }
                ?.let { TriviaDifficulty.valueOf(it) }
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

        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putStringSet(KEY_STATS, encoded)
        }
    }

    private fun buildQuestionSet(anime: Anime): Map<TriviaDifficulty, List<TriviaQuestion>> =
        mapOf(
            TriviaDifficulty.EASY to listOf(
                buildDurationQuestion(anime),
                buildStatusQuestion(anime),
                buildCulturalFoodQuestion(anime)
            ),
            TriviaDifficulty.MEDIUM to listOf(
                buildCulturalTraditionQuestion(anime),
                buildReleaseYearQuestion(anime),
                buildEpisodesQuestion(anime),
                buildMainGenreQuestion(anime),
                buildReleaseDecadeQuestion(anime)
            ),
            TriviaDifficulty.HARD to listOf(
                buildStatementQuestion(anime),
                buildMissingGenreQuestion(anime),
                buildBingeTimeQuestion(anime),
                buildGenresCountQuestion(anime),
                buildAverageMinutesQuestion(anime),
                buildReleaseTimelineQuestion(anime),
                buildPrimaryGenreQuestion(anime),
                buildStatusInferenceQuestion(anime)
            )
        )

    private fun buildDurationQuestion(anime: Anime): TriviaQuestion {
        val options = DurationType.entries.map { it.toReadableText() }
        val correctIndex = options.indexOf(anime.durationType.toReadableText())

        return TriviaQuestion(
            id = "${anime.id}_duration",
            animeId = anime.id,
            difficulty = TriviaDifficulty.EASY,
            question = "¿Qué duración aproximada tienen los episodios de ${anime.title}?",
            options = options,
            correctAnswerIndex = correctIndex,
            feedback = "La serie se considera ${anime.durationType.toReadableText()} por la extensión de cada capítulo."
        )
    }

    private fun buildStatusQuestion(anime: Anime): TriviaQuestion {
        val options = EmissionStatus.entries.map { it.toReadableText() }
        val correctIndex = options.indexOf(anime.emissionStatus.toReadableText())

        return TriviaQuestion(
            id = "${anime.id}_status",
            animeId = anime.id,
            difficulty = TriviaDifficulty.EASY,
            question = "¿Cuál es el estado de emisión actual de ${anime.title}?",
            options = options,
            correctAnswerIndex = correctIndex,
            feedback = "Actualmente la serie se encuentra ${anime.emissionStatus.toReadableText().lowercase()}"
        )
    }

    private fun buildCulturalFoodQuestion(anime: Anime): TriviaQuestion {
        val preference = culturalFoodPreferences[anime.id] ?: defaultFoodPreference
        val correctIndex = foodTypeOptions.indexOf(preference.typeLabel)

        return TriviaQuestion(
            id = "${anime.id}_cultural_food",
            animeId = anime.id,
            difficulty = TriviaDifficulty.EASY,
            question = "A ${preference.character} en ${anime.title} le encanta ${preference.itemName}; ¿qué tipo de comida japonesa es?",
            options = foodTypeOptions,
            correctAnswerIndex = correctIndex,
            feedback = "Se trata de ${preference.typeLabel.lowercase()} y refleja cómo ${preference.character} ${preference.context}"
        )
    }

    private fun buildReleaseYearQuestion(anime: Anime): TriviaQuestion {
        val baseYear = anime.releaseYear ?: 2015
        val options = listOf(baseYear, baseYear + 1, baseYear - 2, baseYear + 3)
            .map { it.coerceAtLeast(1990) }
            .distinct()
            .take(4)
            .shuffled()
            .map { it.toString() }

        val correctIndex = options.indexOf(baseYear.toString())

        return TriviaQuestion(
            id = "${anime.id}_release",
            animeId = anime.id,
            difficulty = TriviaDifficulty.MEDIUM,
            question = "¿En qué año se estrenó ${anime.title}?",
            options = options,
            correctAnswerIndex = correctIndex,
            feedback = "El estreno original ocurrió en $baseYear, marcando su llegada a la TV japonesa"
        )
    }

    private fun buildEpisodesQuestion(anime: Anime): TriviaQuestion {
        val total = anime.totalEpisodes ?: 12
        val options = listOf(total, total + 10, max(1, total - 8), total + 4)
            .map { it.coerceAtLeast(1) }
            .distinct()
            .take(4)
            .shuffled()
            .map { "$it episodios" }

        val correctIndex = options.indexOf("$total episodios")

        return TriviaQuestion(
            id = "${anime.id}_episodes",
            animeId = anime.id,
            difficulty = TriviaDifficulty.MEDIUM,
            question = "¿Cuántos episodios tiene ${anime.title}?",
            options = options,
            correctAnswerIndex = correctIndex,
            feedback = "Hasta la fecha cuenta con $total episodios publicados"
        )
    }

    private fun buildCulturalTraditionQuestion(anime: Anime): TriviaQuestion {
        val highlight = culturalTraditionMoments[anime.id] ?: defaultTradition
        val options = (listOf(highlight.correctConcept) + highlight.distractors).shuffled()
        val correctIndex = options.indexOf(highlight.correctConcept)

        return TriviaQuestion(
            id = "${anime.id}_cultural_tradition",
            animeId = anime.id,
            difficulty = TriviaDifficulty.MEDIUM,
            question = "En ${anime.title}, ${highlight.context}; ¿a qué tradición japonesa hace referencia?",
            options = options,
            correctAnswerIndex = correctIndex,
            feedback = "${highlight.detail}."
        )
    }

    private fun questionCountForDifficulty(difficulty: TriviaDifficulty): Int = when (difficulty) {
        TriviaDifficulty.EASY -> EASY_QUESTION_COUNT
        TriviaDifficulty.MEDIUM -> MEDIUM_QUESTION_COUNT
        TriviaDifficulty.HARD -> HARD_QUESTION_COUNT
    }

    private fun buildGenreOptions(correct: String): List<String> {
        val distractors = FakeDataSource.genres
            .map { it.name }
            .filter { !it.equals(correct, ignoreCase = true) }
            .distinct()
            .shuffled()
            .take(3)

        return (distractors + correct).shuffled()
    }

    private fun buildNumericOptions(
        correct: Int,
        candidates: List<Int>,
        minValue: Int = 1
    ): List<Int> {
        val values = linkedSetOf(correct.coerceAtLeast(minValue))
        candidates.forEach { values += it.coerceAtLeast(minValue) }

        var fallback = (correct + 1).coerceAtLeast(minValue)
        while (values.size < 4) {
            values += fallback
            fallback += 1
        }

        return values.take(4).shuffled()
    }

    private fun buildMainGenreQuestion(anime: Anime): TriviaQuestion {
        val mainGenre = anime.genres.firstOrNull()?.name
            ?: FakeDataSource.genres.firstOrNull()?.name
            ?: "Acción"

        val options = buildGenreOptions(mainGenre)
        val correctIndex = options.indexOf(mainGenre)

        return TriviaQuestion(
            id = "${anime.id}_main_genre",
            animeId = anime.id,
            difficulty = TriviaDifficulty.MEDIUM,
            question = "¿Cuál de estos géneros está más asociado a ${anime.title}?",
            options = options,
            correctAnswerIndex = correctIndex,
            feedback = "$mainGenre forma parte importante de la identidad de ${anime.title}"
        )
    }

    private fun buildReleaseDecadeQuestion(anime: Anime): TriviaQuestion {
        val year = anime.releaseYear ?: 2015
        val decadeStart = (year / 10) * 10
        val correctLabel = "Década del $decadeStart"

        val options = listOf(
            "Década del $decadeStart",
            "Década del ${decadeStart - 10}",
            "Década del ${decadeStart + 10}",
            "Década del ${decadeStart + 20}"
        ).distinct().shuffled()

        val correctIndex = options.indexOf(correctLabel)

        return TriviaQuestion(
            id = "${anime.id}_release_decade",
            animeId = anime.id,
            difficulty = TriviaDifficulty.MEDIUM,
            question = "¿En qué década se estrenó ${anime.title}?",
            options = options,
            correctAnswerIndex = correctIndex,
            feedback = "${anime.title} debutó en la década del $decadeStart"
        )
    }

    private fun buildStatementQuestion(anime: Anime): TriviaQuestion {
        val baseYear = anime.releaseYear ?: 2015
        val mainGenre = anime.genres.firstOrNull()?.name ?: "acción"

        val statements = listOf(
            "${anime.title} mezcla el género $mainGenre con elementos históricos y se estrenó en $baseYear",
            "${anime.title} finalizó en 2010 y es recordado como una comedia romántica",
            "${anime.title} se caracteriza por episodios de menos de 10 minutos estrenados en 2022",
            "${anime.title} nunca se transmitió en TV y sólo existe como película"
        )

        return TriviaQuestion(
            id = "${anime.id}_statement",
            animeId = anime.id,
            difficulty = TriviaDifficulty.HARD,
            question = "Selecciona la afirmación correcta sobre ${anime.title}",
            options = statements,
            correctAnswerIndex = 0,
            feedback = "Su estreno en $baseYear consolidó a ${anime.title} dentro del género $mainGenre"
        )
    }

    private fun buildMissingGenreQuestion(anime: Anime): TriviaQuestion {
        val availableGenres = anime.genres.map { it.name }
        val extraGenre = FakeDataSource.genres
            .firstOrNull { it.name !in availableGenres }
            ?.name
            ?: "Comedia"

        val options = (availableGenres + extraGenre).shuffled()
        val correctIndex = options.indexOf(extraGenre)

        return TriviaQuestion(
            id = "${anime.id}_missing_genre",
            animeId = anime.id,
            difficulty = TriviaDifficulty.HARD,
            question = "¿Cuál de estos géneros NO está asociado a ${anime.title}?",
            options = options,
            correctAnswerIndex = correctIndex,
            feedback = "El género $extraGenre no forma parte de la mezcla principal de la serie"
        )
    }

    private fun buildBingeTimeQuestion(anime: Anime): TriviaQuestion {
        val totalEpisodes = anime.totalEpisodes ?: 12
        val minutesPerEpisode = anime.durationType.toAverageMinutes()
        val totalHours = ceil(totalEpisodes * minutesPerEpisode / 60.0).toInt()

        val options = listOf(
            totalHours,
            totalHours + 4,
            max(1, totalHours - 3),
            totalHours + 2
        ).distinct().shuffled().map { "$it horas" }

        val correctIndex = options.indexOf("$totalHours horas")

        return TriviaQuestion(
            id = "${anime.id}_binge",
            animeId = anime.id,
            difficulty = TriviaDifficulty.HARD,
            question = "Si vieras todos los episodios seguidos, ¿cuántas horas aproximadas invertirías?",
            options = options,
            correctAnswerIndex = correctIndex,
            feedback = "Son alrededor de $totalHours horas de contenido contando los ${anime.totalEpisodes ?: ""} episodios"
        )
    }

    private fun buildGenresCountQuestion(anime: Anime): TriviaQuestion {
        val totalGenres = anime.genres.size.coerceAtLeast(1)
        val numericOptions = buildNumericOptions(
            correct = totalGenres,
            candidates = listOf(totalGenres + 1, totalGenres - 1, totalGenres + 2)
        )

        val options = numericOptions.map { "$it géneros" }
        val correctIndex = options.indexOf("$totalGenres géneros")

        return TriviaQuestion(
            id = "${anime.id}_genres_count",
            animeId = anime.id,
            difficulty = TriviaDifficulty.HARD,
            question = "¿Con cuántos géneros aparece clasificado ${anime.title}?",
            options = options,
            correctAnswerIndex = correctIndex,
            feedback = "${anime.title} aparece asociado a $totalGenres géneros en su ficha actual"
        )
    }

    private fun buildAverageMinutesQuestion(anime: Anime): TriviaQuestion {
        val averageMinutes = anime.durationType.toAverageMinutes()
        val numericOptions = buildNumericOptions(
            correct = averageMinutes,
            candidates = listOf(averageMinutes + 8, averageMinutes - 7, averageMinutes + 15),
            minValue = 5
        )

        val options = numericOptions.map { "$it minutos" }
        val correctIndex = options.indexOf("$averageMinutes minutos")

        return TriviaQuestion(
            id = "${anime.id}_average_minutes",
            animeId = anime.id,
            difficulty = TriviaDifficulty.HARD,
            question = "¿Cuál es la duración promedio más cercana de un episodio de ${anime.title}?",
            options = options,
            correctAnswerIndex = correctIndex,
            feedback = "La duración promedio más cercana para ${anime.title} es de $averageMinutes minutos"
        )
    }

    private fun buildReleaseTimelineQuestion(anime: Anime): TriviaQuestion {
        val year = anime.releaseYear ?: 2015
        val correctLabel = when {
            year < 2010 -> "Antes de 2010"
            year <= 2015 -> "Entre 2010 y 2015"
            year <= 2020 -> "Entre 2016 y 2020"
            else -> "Después de 2020"
        }

        val options = listOf(
            "Antes de 2010",
            "Entre 2010 y 2015",
            "Entre 2016 y 2020",
            "Después de 2020"
        )

        val correctIndex = options.indexOf(correctLabel)

        return TriviaQuestion(
            id = "${anime.id}_release_timeline",
            animeId = anime.id,
            difficulty = TriviaDifficulty.HARD,
            question = "¿En qué tramo de tiempo se ubica el estreno de ${anime.title}?",
            options = options,
            correctAnswerIndex = correctIndex,
            feedback = "El estreno de ${anime.title} cae en el rango: $correctLabel"
        )
    }

    private fun buildPrimaryGenreQuestion(anime: Anime): TriviaQuestion {
        val mainGenre = anime.genres.firstOrNull()?.name
            ?: FakeDataSource.genres.firstOrNull()?.name
            ?: "Acción"

        val options = buildGenreOptions(mainGenre)
        val correctIndex = options.indexOf(mainGenre)

        return TriviaQuestion(
            id = "${anime.id}_primary_genre",
            animeId = anime.id,
            difficulty = TriviaDifficulty.HARD,
            question = "Si tuvieras que resumir ${anime.title} con su género principal, ¿cuál elegirías?",
            options = options,
            correctAnswerIndex = correctIndex,
            feedback = "El género principal más representativo aquí es $mainGenre"
        )
    }

    private fun buildStatusInferenceQuestion(anime: Anime): TriviaQuestion {
        val correctLabel = anime.emissionStatus.toReadableText()
        val options = EmissionStatus.entries.map { it.toReadableText() }
        val correctIndex = options.indexOf(correctLabel)

        return TriviaQuestion(
            id = "${anime.id}_status_inference",
            animeId = anime.id,
            difficulty = TriviaDifficulty.HARD,
            question = "Pensando en su publicación actual, ¿qué etiqueta describe mejor el estado de ${anime.title}?",
            options = options,
            correctAnswerIndex = correctIndex,
            feedback = "${anime.title} está ${correctLabel.lowercase()} dentro de su estado de emisión"
        )
    }

    private fun DurationType.toReadableText(): String = when (this) {
        DurationType.SHORT -> "Corto (≤15 min)"
        DurationType.MEDIUM -> "Medio (16-25 min)"
        DurationType.LONG -> "Largo (30+ min)"
    }

    private fun DurationType.toAverageMinutes(): Int = when (this) {
        DurationType.SHORT -> 12
        DurationType.MEDIUM -> 23
        DurationType.LONG -> 35
    }

    private fun EmissionStatus.toReadableText(): String = when (this) {
        EmissionStatus.ON_AIR -> "En emisión"
        EmissionStatus.FINISHED -> "Finalizado"
        EmissionStatus.ON_BREAK -> "En pausa"
    }
}