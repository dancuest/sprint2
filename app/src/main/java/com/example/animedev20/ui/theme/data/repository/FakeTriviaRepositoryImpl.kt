package com.example.animedev20.ui.theme.data.repository


import com.example.animedev20.ui.theme.data.FakeDataSource
import com.example.animedev20.ui.theme.domain.model.Anime
import com.example.animedev20.ui.theme.domain.model.DurationType
import com.example.animedev20.ui.theme.domain.model.EmissionStatus
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaDifficulty
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaQuestion
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaSummary
import com.example.animedev20.ui.theme.domain.repository.TriviaRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlin.math.ceil
import kotlin.math.max

object FakeTriviaRepositoryImpl : TriviaRepository {

    private const val DEFAULT_QUESTION_COUNT = 3

    private data class TriviaStats(
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

    private val statsFlow = MutableStateFlow<Map<Long, TriviaStats>>(emptyMap())

    private val foodTypeOptions = listOf("Comida de mar", "Dulces", "Comida salada", "Bebidas")

    private val culturalFoodPreferences: Map<Long, CulturalFoodPreference> = mapOf(
        1L to CulturalFoodPreference(
            character = "Tanjiro",
            itemName = "mitarashi dango",
            typeLabel = "Dulces",
            context = "lo comparte con Nezuko en las calles del mercado"
        ),
        2L to CulturalFoodPreference(
            character = "Thorfinn",
            itemName = "onigiri relleno",
            typeLabel = "Comida salada",
            context = "recuerda los bocadillos que probó junto a mercaderes japoneses"
        ),
        3L to CulturalFoodPreference(
            character = "Riko",
            itemName = "dorayaki",
            typeLabel = "Dulces",
            context = "lo prepara como merienda antes de descender al Abismo"
        ),
        4L to CulturalFoodPreference(
            character = "el profesor Gojo",
            itemName = "taiyaki",
            typeLabel = "Dulces",
            context = "no puede resistirse a comprarlos entre misiones"
        ),
        5L to CulturalFoodPreference(
            character = "el Dr. Tenma",
            itemName = "té matcha",
            typeLabel = "Bebidas",
            context = "lo utiliza para recordar sus raíces japonesas en medio de Europa"
        ),
        6L to CulturalFoodPreference(
            character = "Winry",
            itemName = "nikuman al vapor",
            typeLabel = "Comida salada",
            context = "los comparte con los hermanos Elric tras las reparaciones"
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
            detail = "Los yorishiro simbolizan objetos que atraen a los kami para resguardar a los presentes"
        ),
        3L to CulturalTraditionMoment(
            context = "en las festividades que los exploradores recrean antes de descender",
            correctConcept = "un pequeño matsuri dedicado a desear buena fortuna",
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

    private val questionBank: Map<Long, Map<TriviaDifficulty, List<TriviaQuestion>>> =
        FakeDataSource.animeCatalog.associate { anime ->
            anime.id to buildQuestionSet(anime)
        }

    override fun getTriviaSummaries(): Flow<List<TriviaSummary>> = statsFlow.map { stats ->
        FakeDataSource.animeCatalog.map { anime ->
            val animeStats = stats[anime.id]
            TriviaSummary(
                anime = anime,
                lastScore = animeStats?.lastScore,
                totalQuestions = animeStats?.totalQuestions ?: DEFAULT_QUESTION_COUNT,
                lastDifficulty = animeStats?.lastDifficulty,
                bestScore = animeStats?.bestScore ?: 0
            )
        }
    }

    override suspend fun getQuestions(
        animeId: Long,
        difficulty: TriviaDifficulty
    ): List<TriviaQuestion>
    {
        delay(400)
        val questionsByDifficulty = questionBank[animeId]
            ?: error("No hay trivias disponibles para el anime con id $animeId")
        return questionsByDifficulty[difficulty]
            ?: error("No hay preguntas para la dificultad $difficulty")
    }

    override suspend fun recordResult(
        animeId: Long,
        difficulty: TriviaDifficulty,
        score: Int,
        totalQuestions: Int
    ) {
        val current = statsFlow.value[animeId]
        val updated = TriviaStats(
            lastScore = score,
            totalQuestions = totalQuestions,
            lastDifficulty = difficulty,
            bestScore = max(current?.bestScore ?: 0, score)
        )
        statsFlow.value = statsFlow.value + (animeId to updated)
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
            ),
            TriviaDifficulty.HARD to listOf(
                buildStatementQuestion(anime),
                buildMissingGenreQuestion(anime),
                buildBingeTimeQuestion(anime)
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
            feedback = "Actualmente la serie se encuentra ${
                anime.emissionStatus.toReadableText().lowercase()
            }"
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
        val options = listOf(
            baseYear,
            baseYear + 1,
            baseYear - 2,
            baseYear + 3
        ).map { it.coerceAtLeast(1990) }.distinct().take(4).map { it.toString() }
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
        val options = listOf(
            total,
            total + 10,
            max(1, total - 8),
            total + 4
        ).map { it.coerceAtLeast(1) }.distinct().take(4).map { "$it episodios" }
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

    private fun buildStatementQuestion(anime: Anime): TriviaQuestion {
        val baseYear = anime.releaseYear ?: 2015
        val mainGenre = anime.genres.firstOrNull()?.name ?: "acción"
        val statements = listOf(
            "${anime.title} mezcla el género $mainGenre con elementos históricos y se estrenó en $baseYear",
            "${anime.title} finalizó en 2010 y es recordado como una comedia romántica",
            "${anime.title} se caracteriza por episodios de menos de 10 minutos estrenados en 2022",
            "${anime.title} nunca se transmitió en TV y sólo existe como película"
        )
        val correctIndex = 0
        return TriviaQuestion(
            id = "${anime.id}_statement",
            animeId = anime.id,
            difficulty = TriviaDifficulty.HARD,
            question = "Selecciona la afirmación correcta sobre ${anime.title}",
            options = statements,
            correctAnswerIndex = correctIndex,
            feedback = "Su estreno en $baseYear consolidó a ${anime.title} dentro del género $mainGenre"
        )
    }

    private fun buildMissingGenreQuestion(anime: Anime): TriviaQuestion {
        val availableGenres = anime.genres.map { it.name }
        val extraGenre =
            FakeDataSource.genres.firstOrNull { it.name !in availableGenres }?.name ?: "Comedia"
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
        ).distinct().map { "$it horas" }
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