package com.example.animedev20.ui.theme.data.trivia

import com.example.animedev20.ui.theme.domain.model.Anime
import com.example.animedev20.ui.theme.domain.model.DurationType
import com.example.animedev20.ui.theme.domain.model.EmissionStatus
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaDifficulty
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaQuestion
import kotlin.math.ceil
import kotlin.math.max

object AnimeTriviaQuestionFactory {

    private const val EASY_QUESTION_COUNT = 3
    private const val MEDIUM_QUESTION_COUNT = 5
    private const val HARD_QUESTION_COUNT = 8

    private val genreDistractors = listOf(
        "Shonen", "Seinen", "Shojo", "Josei",
        "Comedy", "Romance", "Sports", "Mecha",
        "Isekai", "Horror", "Music", "Slice of Life",
        "Fantasy", "Sci-Fi", "Supernatural", "Mystery",
        "Drama", "Adventure", "Action", "Gourmet",
        "Avant Garde", "Suspense", "Award Winning"
    )

    private val titleDistractors = listOf(
        "Naruto",
        "Bleach",
        "Dragon Ball Z",
        "Death Note",
        "Attack on Titan",
        "Hunter x Hunter",
        "Cowboy Bebop",
        "Spy x Family",
        "Chainsaw Man",
        "My Hero Academia",
        "Fullmetal Alchemist: Brotherhood",
        "Demon Slayer"
    )

    private val synopsisDistractors = listOf(
        "piratas espaciales",
        "torneo escolar",
        "robot gigante",
        "café mágico",
        "detective fantasma",
        "reino submarino",
        "academia musical",
        "viaje culinario",
        "guerra galáctica",
        "club secreto"
    )

    private val stopWords = setOf(
        "the", "and", "with", "that", "this", "from", "into", "when", "where",
        "their", "there", "about", "after", "before", "while", "they", "them",
        "his", "her", "she", "him", "you", "for", "are", "was", "were", "but",
        "not", "has", "have", "had", "all", "new", "one", "two",
        "que", "para", "con", "una", "unos", "unas", "por", "del", "las",
        "los", "sus", "este", "esta", "estos", "estas", "como", "más", "muy",
        "sin", "sobre", "entre", "cuando", "donde", "quien", "cada", "todo",
        "toda", "todos", "todas", "ser", "son", "fue", "era", "han", "hay"
    )

    fun questionCountForDifficulty(difficulty: TriviaDifficulty): Int = when (difficulty) {
        TriviaDifficulty.EASY -> EASY_QUESTION_COUNT
        TriviaDifficulty.MEDIUM -> MEDIUM_QUESTION_COUNT
        TriviaDifficulty.HARD -> HARD_QUESTION_COUNT
    }

    fun build(anime: Anime, difficulty: TriviaDifficulty): List<TriviaQuestion> {
        val pool = when (difficulty) {
            TriviaDifficulty.EASY -> buildEasyPool(anime)
            TriviaDifficulty.MEDIUM -> buildMediumPool(anime)
            TriviaDifficulty.HARD -> buildHardPool(anime)
        }

        return pool
            .distinctBy { it.id }
            .shuffled()
            .take(questionCountForDifficulty(difficulty))
    }

    private fun buildEasyPool(anime: Anime): List<TriviaQuestion> = listOfNotNull(
        buildTitleQuestion(anime),
        buildStatusQuestion(anime, TriviaDifficulty.EASY),
        buildDurationQuestion(anime, TriviaDifficulty.EASY),
        buildMainGenreQuestion(anime, TriviaDifficulty.EASY),
        buildResourceQuestion(anime, TriviaDifficulty.EASY),
        buildTitleLengthQuestion(anime, TriviaDifficulty.EASY),
        buildSynopsisKeywordQuestion(anime, TriviaDifficulty.EASY)
    )

    private fun buildMediumPool(anime: Anime): List<TriviaQuestion> = listOfNotNull(
        buildReleaseYearQuestion(anime),
        buildEpisodesQuestion(anime),
        buildUnknownEpisodesQuestion(anime, TriviaDifficulty.MEDIUM),
        buildMainGenreQuestion(anime, TriviaDifficulty.MEDIUM),
        buildReleaseDecadeQuestion(anime),
        buildOriginalTitleQuestion(anime, TriviaDifficulty.MEDIUM),
        buildResourceQuestion(anime, TriviaDifficulty.MEDIUM),
        buildGenresCountQuestion(anime, TriviaDifficulty.MEDIUM),
        buildSynopsisKeywordQuestion(anime, TriviaDifficulty.MEDIUM),
        buildSynopsisSizeQuestion(anime, TriviaDifficulty.MEDIUM),
        buildCatalogIdentityQuestion(anime, TriviaDifficulty.MEDIUM),
        buildFormatDecisionQuestion(anime, TriviaDifficulty.MEDIUM)
    )

    private fun buildHardPool(anime: Anime): List<TriviaQuestion> = listOfNotNull(
        buildCorrectStatementQuestion(anime),
        buildMissingGenreQuestion(anime),
        buildBingeTimeQuestion(anime),
        buildUnknownEpisodesQuestion(anime, TriviaDifficulty.HARD),
        buildAverageMinutesQuestion(anime),
        buildReleaseTimelineQuestion(anime),
        buildPrimaryGenreQuestion(anime),
        buildEpisodeScaleQuestion(anime),
        buildOriginalTitleQuestion(anime, TriviaDifficulty.HARD),
        buildGenresCountQuestion(anime, TriviaDifficulty.HARD),
        buildSynopsisDoubleKeywordQuestion(anime),
        buildCatalogIdentityQuestion(anime, TriviaDifficulty.HARD),
        buildFormatDecisionQuestion(anime, TriviaDifficulty.HARD),
        buildDataIntegrityQuestion(anime)
    )

    private fun buildTitleQuestion(anime: Anime): TriviaQuestion {
        return buildQuestion(
            id = "${anime.id}_personality_title",
            anime = anime,
            difficulty = TriviaDifficulty.EASY,
            question = "Modo calentamiento: ¿qué anime está en el centro de esta trivia?",
            correctOption = anime.title,
            distractors = titleDistractors.filterNot {
                it.equals(anime.title, ignoreCase = true)
            },
            feedback = "Exacto. Esta ronda está dedicada a ${anime.title}; no a un anime infiltrado de otro multiverso."
        )
    }

    private fun buildStatusQuestion(
        anime: Anime,
        difficulty: TriviaDifficulty
    ): TriviaQuestion {
        val correct = anime.emissionStatus.toReadableText()

        return buildQuestion(
            id = "${anime.id}_personality_status_${difficulty.name.lowercase()}",
            anime = anime,
            difficulty = difficulty,
            question = when (difficulty) {
                TriviaDifficulty.EASY ->
                    "Semáforo anime: ¿qué estado de emisión tiene ${anime.title}?"

                TriviaDifficulty.MEDIUM ->
                    "Si ${anime.title} estuviera en tu lista de seguimiento, ¿qué etiqueta de emisión verías?"

                TriviaDifficulty.HARD ->
                    "Sin caer en teorías de fandom: ¿cuál es el estado de emisión registrado para ${anime.title}?"
            },
            correctOption = correct,
            distractors = EmissionStatus.entries
                .map { it.toReadableText() }
                .filterNot { it == correct },
            feedback = "La ficha registra a ${anime.title} como ${correct.lowercase()}."
        )
    }

    private fun buildDurationQuestion(
        anime: Anime,
        difficulty: TriviaDifficulty
    ): TriviaQuestion {
        val correct = anime.durationType.toReadableText()

        return buildQuestion(
            id = "${anime.id}_personality_duration_${difficulty.name.lowercase()}",
            anime = anime,
            difficulty = difficulty,
            question = when (difficulty) {
                TriviaDifficulty.EASY ->
                    "Ritmo de episodio: ¿qué duración aproximada maneja ${anime.title}?"

                TriviaDifficulty.MEDIUM ->
                    "Antes de darle play a ${anime.title}, ¿qué tan largos pinta sus episodios la ficha?"

                TriviaDifficulty.HARD ->
                    "Pensando como planner de maratón, ¿qué categoría de duración usa ${anime.title}?"
            },
            correctOption = correct,
            distractors = DurationType.entries
                .map { it.toReadableText() }
                .filterNot { it == correct },
            feedback = "La duración se calcula desde la categoría registrada, no desde una escena inventada."
        )
    }

    private fun buildReleaseYearQuestion(anime: Anime): TriviaQuestion? {
        val year = anime.releaseYear ?: return null

        return buildQuestion(
            id = "${anime.id}_personality_release_year",
            anime = anime,
            difficulty = TriviaDifficulty.MEDIUM,
            question = "Viaje en el tiempo: ¿en qué año aterrizó ${anime.title} según su ficha?",
            correctOption = year.toString(),
            distractors = numericDistractors(
                correct = year,
                candidates = listOf(year + 1, year - 1, year + 3, year - 3),
                minValue = 1960
            ).map { it.toString() },
            feedback = "${anime.title} registra como año de estreno: $year."
        )
    }

    private fun buildEpisodesQuestion(anime: Anime): TriviaQuestion? {
        val total = anime.totalEpisodes ?: return null
        val step = episodeDistractorStep(total)

        return buildQuestion(
            id = "${anime.id}_personality_episodes",
            anime = anime,
            difficulty = TriviaDifficulty.MEDIUM,
            question = "Contador de capítulos: ¿cuántos episodios registra la ficha de ${anime.title}?",
            correctOption = "$total episodios",
            distractors = numericDistractors(
                correct = total,
                candidates = listOf(
                    total + step,
                    max(1, total - step),
                    total + (step * 2),
                    max(1, total - (step * 2))
                ),
                minValue = 1
            ).map { "$it episodios" },
            feedback = "Esta pregunta solo aparece porque la ficha sí trae un total de episodios registrado."
        )
    }

    private fun buildUnknownEpisodesQuestion(
        anime: Anime,
        difficulty: TriviaDifficulty
    ): TriviaQuestion? {
        if (anime.totalEpisodes != null) return null

        return buildQuestion(
            id = "${anime.id}_personality_unknown_episodes_${difficulty.name.lowercase()}",
            anime = anime,
            difficulty = difficulty,
            question = "El contador de episodios de ${anime.title} está en modo incógnito. ¿Qué significa eso?",
            correctOption = "La ficha no trae una cantidad confirmada de episodios",
            distractors = listOf(
                "Que el anime tiene exactamente 12 episodios",
                "Que la trivia debe copiar episodios de otro anime",
                "Que el anime no tiene capítulos",
                "Que solo existe en formato película"
            ),
            feedback = "Correcto. Si la fuente no trae el dato, AnimeDev no lo inventa. Esa es la jugada limpia."
        )
    }

    private fun buildMainGenreQuestion(
        anime: Anime,
        difficulty: TriviaDifficulty
    ): TriviaQuestion? {
        val mainGenre = anime.genres.firstOrNull()?.name ?: return null

        return buildQuestion(
            id = "${anime.id}_personality_main_genre_${difficulty.name.lowercase()}",
            anime = anime,
            difficulty = difficulty,
            question = when (difficulty) {
                TriviaDifficulty.EASY ->
                    "Radar de género: ¿cuál de estos géneros sí aparece asociado a ${anime.title}?"

                TriviaDifficulty.MEDIUM ->
                    "ADN del catálogo: ¿qué género forma parte de la identidad registrada de ${anime.title}?"

                TriviaDifficulty.HARD ->
                    "Mirando la ficha sin fan theories, ¿qué género está realmente conectado con ${anime.title}?"
            },
            correctOption = mainGenre,
            distractors = genreDistractors.filterNotIn(anime.genres.map { it.name }),
            feedback = "$mainGenre aparece dentro de los géneros registrados para ${anime.title}."
        )
    }

    private fun buildReleaseDecadeQuestion(anime: Anime): TriviaQuestion? {
        val year = anime.releaseYear ?: return null
        val decade = (year / 10) * 10
        val correct = "Década de $decade"

        return buildQuestion(
            id = "${anime.id}_personality_release_decade",
            anime = anime,
            difficulty = TriviaDifficulty.MEDIUM,
            question = "Era anime: ¿en qué década cae el estreno de ${anime.title}?",
            correctOption = correct,
            distractors = listOf(
                "Década de ${decade - 10}",
                "Década de ${decade + 10}",
                "Década de ${decade + 20}"
            ),
            feedback = "El año registrado es $year, así que ${anime.title} cae en la $correct."
        )
    }

    private fun buildOriginalTitleQuestion(
        anime: Anime,
        difficulty: TriviaDifficulty
    ): TriviaQuestion? {
        val original = anime.originalTitle
            ?.takeIf { it.isNotBlank() }
            ?.takeIf { !it.equals(anime.title, ignoreCase = true) }
            ?: return null

        return buildQuestion(
            id = "${anime.id}_personality_original_title_${difficulty.name.lowercase()}",
            anime = anime,
            difficulty = difficulty,
            question = when (difficulty) {
                TriviaDifficulty.EASY ->
                    "Nombre alterno desbloqueado: ¿cuál es el título original registrado?"

                TriviaDifficulty.MEDIUM ->
                    "Alias oficial de ${anime.title}: ¿qué título original aparece en la ficha?"

                TriviaDifficulty.HARD ->
                    "Sin traducciones creativas de internet: ¿cuál es el título original registrado para ${anime.title}?"
            },
            correctOption = original,
            distractors = listOf(
                anime.title,
                "Título no registrado",
                "Versión alternativa sin confirmar",
                "Nombre pendiente de traducción"
            ),
            feedback = "La ficha registra como título original: $original."
        )
    }

    private fun buildResourceQuestion(
        anime: Anime,
        difficulty: TriviaDifficulty
    ): TriviaQuestion {
        val correct = when {
            anime.trailerUrl != null && anime.mangaUrl != null -> "Trailer oficial y manga relacionado"
            anime.trailerUrl != null -> "Trailer oficial"
            anime.mangaUrl != null -> "Manga relacionado"
            else -> "Ningún recurso complementario registrado"
        }

        return buildQuestion(
            id = "${anime.id}_personality_resources_${difficulty.name.lowercase()}",
            anime = anime,
            difficulty = difficulty,
            question = when (difficulty) {
                TriviaDifficulty.EASY ->
                    "Zona de extras: ¿qué recurso complementario tiene disponible ${anime.title}?"

                TriviaDifficulty.MEDIUM ->
                    "Si abres la ficha de ${anime.title}, ¿qué bonus de contenido deberías encontrar?"

                TriviaDifficulty.HARD ->
                    "Modo auditor de ficha: ¿qué recurso extra está realmente registrado para ${anime.title}?"
            },
            correctOption = correct,
            distractors = listOf(
                "Solo videojuego oficial",
                "Solo novela visual",
                "Banda sonora interactiva",
                "Ningún recurso complementario registrado",
                "Trailer oficial",
                "Manga relacionado",
                "Trailer oficial y manga relacionado"
            ).filterNot { it == correct },
            feedback = "Esta respuesta sale de los enlaces registrados en la ficha, no de suposiciones."
        )
    }

    private fun buildGenresCountQuestion(
        anime: Anime,
        difficulty: TriviaDifficulty
    ): TriviaQuestion {
        val totalGenres = anime.genres.size

        return buildQuestion(
            id = "${anime.id}_personality_genres_count_${difficulty.name.lowercase()}",
            anime = anime,
            difficulty = difficulty,
            question = when (difficulty) {
                TriviaDifficulty.EASY ->
                    "Conteo rápido: ¿cuántos géneros tiene ${anime.title} en su ficha?"

                TriviaDifficulty.MEDIUM ->
                    "Inventario de vibes: ¿cuántos géneros aparecen asociados a ${anime.title}?"

                TriviaDifficulty.HARD ->
                    "Sin inflar la ficha: ¿cuántos géneros registrados tiene exactamente ${anime.title}?"
            },
            correctOption = "$totalGenres géneros",
            distractors = numericDistractors(
                correct = totalGenres,
                candidates = listOf(totalGenres + 1, max(0, totalGenres - 1), totalGenres + 2),
                minValue = 0
            ).map { "$it géneros" },
            feedback = "${anime.title} tiene $totalGenres géneros registrados en su ficha."
        )
    }

    private fun buildCorrectStatementQuestion(anime: Anime): TriviaQuestion {
        val status = anime.emissionStatus.toReadableText()
        val yearText = anime.releaseYear?.let { " y se estrenó en $it" }.orEmpty()
        val genreText = anime.genres.firstOrNull()?.name
            ?.let { " dentro del género $it" }
            .orEmpty()

        val correct = "${anime.title} aparece como ${status.lowercase()}$yearText$genreText."

        val wrongStatus = EmissionStatus.entries
            .first { it != anime.emissionStatus }
            .toReadableText()

        val wrongGenre = genreDistractors
            .firstOrNull { candidate ->
                anime.genres.none { it.name.equals(candidate, ignoreCase = true) }
            }
            ?: "Género no asociado"

        val wrongYear = anime.releaseYear?.plus(5)

        val distractors = listOfNotNull(
            "${anime.title} aparece como ${wrongStatus.lowercase()} en su ficha actual.",
            wrongYear?.let { "${anime.title} se estrenó en $it según su ficha." },
            "${anime.title} está clasificado principalmente como $wrongGenre en su ficha actual."
        )

        return buildQuestion(
            id = "${anime.id}_personality_statement",
            anime = anime,
            difficulty = TriviaDifficulty.HARD,
            question = "Boss fight de precisión: elige la afirmación que sí respeta la ficha de ${anime.title}.",
            correctOption = correct,
            distractors = distractors,
            feedback = "La respuesta correcta combina únicamente datos reales de la ficha actual."
        )
    }

    private fun buildMissingGenreQuestion(anime: Anime): TriviaQuestion? {
        val availableGenres = anime.genres.map { it.name }
        if (availableGenres.isEmpty()) return null

        val missingGenre = genreDistractors
            .firstOrNull { candidate ->
                availableGenres.none { it.equals(candidate, ignoreCase = true) }
            }
            ?: return null

        return buildQuestion(
            id = "${anime.id}_personality_missing_genre",
            anime = anime,
            difficulty = TriviaDifficulty.HARD,
            question = "Filtro anti-impostores: ¿cuál de estos géneros NO aparece en la ficha de ${anime.title}?",
            correctOption = missingGenre,
            distractors = availableGenres.take(3),
            feedback = "$missingGenre no aparece dentro de los géneros registrados para ${anime.title}."
        )
    }

    private fun buildBingeTimeQuestion(anime: Anime): TriviaQuestion? {
        val totalEpisodes = anime.totalEpisodes ?: return null
        val minutesPerEpisode = anime.durationType.toAverageMinutes()
        val totalHours = ceil(totalEpisodes * minutesPerEpisode / 60.0).toInt()
        val delta = when {
            totalHours >= 200 -> 40
            totalHours >= 80 -> 16
            totalHours >= 30 -> 8
            else -> 4
        }

        return buildQuestion(
            id = "${anime.id}_personality_binge_time",
            anime = anime,
            difficulty = TriviaDifficulty.HARD,
            question = "Modo maratón irresponsable: ¿cuántas horas aproximadas tomaría ver todo ${anime.title}?",
            correctOption = "$totalHours horas",
            distractors = numericDistractors(
                correct = totalHours,
                candidates = listOf(
                    totalHours + delta,
                    max(1, totalHours - delta),
                    totalHours + (delta * 2),
                    max(1, totalHours - (delta * 2))
                ),
                minValue = 1
            ).map { "$it horas" },
            feedback = "El cálculo usa episodios registrados y duración promedio. Nada de matemática estilo villano."
        )
    }

    private fun buildAverageMinutesQuestion(anime: Anime): TriviaQuestion {
        val averageMinutes = anime.durationType.toAverageMinutes()

        return buildQuestion(
            id = "${anime.id}_personality_average_minutes",
            anime = anime,
            difficulty = TriviaDifficulty.HARD,
            question = "Cronómetro otaku: ¿qué duración promedio usa AnimeDev para estimar un episodio de ${anime.title}?",
            correctOption = "$averageMinutes minutos",
            distractors = numericDistractors(
                correct = averageMinutes,
                candidates = listOf(
                    averageMinutes + 8,
                    max(5, averageMinutes - 8),
                    averageMinutes + 15
                ),
                minValue = 5
            ).map { "$it minutos" },
            feedback = "Es una aproximación basada en la categoría de duración del anime."
        )
    }

    private fun buildReleaseTimelineQuestion(anime: Anime): TriviaQuestion? {
        val year = anime.releaseYear ?: return null

        val correct = when {
            year < 2000 -> "Antes del año 2000"
            year <= 2010 -> "Entre 2000 y 2010"
            year <= 2020 -> "Entre 2011 y 2020"
            else -> "Después de 2020"
        }

        return buildQuestion(
            id = "${anime.id}_personality_release_timeline",
            anime = anime,
            difficulty = TriviaDifficulty.HARD,
            question = "Línea temporal desbloqueada: ¿en qué tramo cae el estreno de ${anime.title}?",
            correctOption = correct,
            distractors = listOf(
                "Antes del año 2000",
                "Entre 2000 y 2010",
                "Entre 2011 y 2020",
                "Después de 2020"
            ).filterNot { it == correct },
            feedback = "El año registrado para ${anime.title} es $year."
        )
    }

    private fun buildPrimaryGenreQuestion(anime: Anime): TriviaQuestion? {
        val primaryGenre = anime.genres.firstOrNull()?.name ?: return null

        return buildQuestion(
            id = "${anime.id}_personality_primary_genre",
            anime = anime,
            difficulty = TriviaDifficulty.HARD,
            question = "Primer golpe de identidad: según el orden de la ficha, ¿cuál es el primer género de ${anime.title}?",
            correctOption = primaryGenre,
            distractors = genreDistractors.filterNotIn(anime.genres.map { it.name }),
            feedback = "$primaryGenre aparece como primer género registrado para ${anime.title}."
        )
    }

    private fun buildEpisodeScaleQuestion(anime: Anime): TriviaQuestion? {
        val total = anime.totalEpisodes ?: return null

        val correct = when {
            total <= 13 -> "Serie corta"
            total <= 26 -> "Serie de una temporada estándar"
            total <= 100 -> "Serie mediana"
            else -> "Serie extensa"
        }

        return buildQuestion(
            id = "${anime.id}_personality_episode_scale",
            anime = anime,
            difficulty = TriviaDifficulty.HARD,
            question = "Por tamaño de misión, ¿cómo se clasifica mejor ${anime.title} según sus episodios registrados?",
            correctOption = correct,
            distractors = listOf(
                "Serie corta",
                "Serie de una temporada estándar",
                "Serie mediana",
                "Serie extensa"
            ).filterNot { it == correct },
            feedback = "${anime.title} registra $total episodios, por eso corresponde a: $correct."
        )
    }

    private fun buildSynopsisKeywordQuestion(
        anime: Anime,
        difficulty: TriviaDifficulty
    ): TriviaQuestion? {
        val keyword = extractSynopsisKeywords(anime.synopsis).firstOrNull()
            ?: return null

        return buildQuestion(
            id = "${anime.id}_personality_synopsis_keyword_${difficulty.name.lowercase()}",
            anime = anime,
            difficulty = difficulty,
            question = when (difficulty) {
                TriviaDifficulty.EASY ->
                    "Pista de sinopsis: ¿qué palabra aparece realmente en el resumen de ${anime.title}?"

                TriviaDifficulty.MEDIUM ->
                    "Modo detective: ¿cuál de estas pistas sí sale del resumen de ${anime.title}?"

                TriviaDifficulty.HARD ->
                    "Lectura fina: ¿qué palabra fue extraída directamente de la sinopsis de ${anime.title}?"
            },
            correctOption = keyword,
            distractors = synopsisDistractors.filterNot { distractor ->
                anime.synopsis.contains(distractor, ignoreCase = true)
            },
            feedback = "La palabra \"$keyword\" aparece en la sinopsis registrada de ${anime.title}."
        )
    }

    private fun buildSynopsisDoubleKeywordQuestion(anime: Anime): TriviaQuestion? {
        val keywords = extractSynopsisKeywords(anime.synopsis)
        if (keywords.size < 2) return null

        val correct = "${keywords[0]} + ${keywords[1]}"

        return buildQuestion(
            id = "${anime.id}_personality_synopsis_double_keyword",
            anime = anime,
            difficulty = TriviaDifficulty.HARD,
            question = "Combo de pistas: ¿qué dupla de palabras aparece realmente en la sinopsis de ${anime.title}?",
            correctOption = correct,
            distractors = listOf(
                "torneo + escolar",
                "piratas + espaciales",
                "robot + gigante",
                "café + mágico",
                "detective + fantasma",
                "reino + submarino"
            ).filterNot { it.equals(correct, ignoreCase = true) },
            feedback = "Ese combo sale de la sinopsis registrada. Aquí no estamos haciendo relleno de temporada."
        )
    }

    private fun buildSynopsisSizeQuestion(
        anime: Anime,
        difficulty: TriviaDifficulty
    ): TriviaQuestion {
        val wordCount = anime.synopsis
            .split(Regex("\\s+"))
            .count { it.isNotBlank() }

        val correct = when {
            wordCount <= 20 -> "Sinopsis breve"
            wordCount <= 60 -> "Sinopsis media"
            else -> "Sinopsis amplia"
        }

        return buildQuestion(
            id = "${anime.id}_personality_synopsis_size_${difficulty.name.lowercase()}",
            anime = anime,
            difficulty = difficulty,
            question = "Tamaño del pitch: por extensión, ¿cómo se clasifica la sinopsis de ${anime.title}?",
            correctOption = correct,
            distractors = listOf(
                "Sinopsis breve",
                "Sinopsis media",
                "Sinopsis amplia",
                "Sinopsis inexistente"
            ).filterNot { it == correct },
            feedback = "La clasificación se calcula por cantidad aproximada de palabras en la sinopsis."
        )
    }

    private fun buildCatalogIdentityQuestion(
        anime: Anime,
        difficulty: TriviaDifficulty
    ): TriviaQuestion {
        val hasYear = anime.releaseYear != null
        val hasGenres = anime.genres.isNotEmpty()
        val hasOriginalTitle = !anime.originalTitle.isNullOrBlank()

        val correct = when {
            hasYear && hasGenres && hasOriginalTitle ->
                "Tiene año, géneros y título original registrados"

            hasYear && hasGenres ->
                "Tiene año y géneros registrados"

            hasGenres && hasOriginalTitle ->
                "Tiene géneros y título original registrados"

            hasYear && hasOriginalTitle ->
                "Tiene año y título original registrados"

            hasGenres ->
                "Tiene géneros registrados"

            hasYear ->
                "Tiene año de estreno registrado"

            hasOriginalTitle ->
                "Tiene título original registrado"

            else ->
                "Tiene ficha básica sin esos detalles extra"
        }

        return buildQuestion(
            id = "${anime.id}_personality_catalog_identity_${difficulty.name.lowercase()}",
            anime = anime,
            difficulty = difficulty,
            question = when (difficulty) {
                TriviaDifficulty.EASY ->
                    "Checklist de ficha: ¿qué datos destacados tiene ${anime.title}?"

                TriviaDifficulty.MEDIUM ->
                    "Modo curador de catálogo: ¿qué combinación describe mejor la ficha de ${anime.title}?"

                TriviaDifficulty.HARD ->
                    "Auditoría de metadata: ¿qué paquete de información está realmente disponible para ${anime.title}?"
            },
            correctOption = correct,
            distractors = listOf(
                "Tiene año, géneros y título original registrados",
                "Tiene año y géneros registrados",
                "Tiene géneros y título original registrados",
                "Tiene año de estreno registrado",
                "Tiene ficha básica sin esos detalles extra"
            ).filterNot { it == correct },
            feedback = "Esta pregunta valida qué datos existen en la ficha, sin maquillar información."
        )
    }

    private fun buildFormatDecisionQuestion(
        anime: Anime,
        difficulty: TriviaDifficulty
    ): TriviaQuestion {
        val correct = anime.durationType.toReadableText()

        return buildQuestion(
            id = "${anime.id}_personality_format_decision_${difficulty.name.lowercase()}",
            anime = anime,
            difficulty = difficulty,
            question = when (difficulty) {
                TriviaDifficulty.EASY ->
                    "Si tienes poco tiempo antes de clase, ¿qué dato te ayuda a decidir si ver un episodio de ${anime.title}?"

                TriviaDifficulty.MEDIUM ->
                    "Decisión de usuario: para calcular si ${anime.title} cabe en una pausa, ¿qué categoría importa?"

                TriviaDifficulty.HARD ->
                    "Pensando en experiencia de usuario, ¿qué dato de ${anime.title} alimenta mejor una estimación de tiempo?"
            },
            correctOption = correct,
            distractors = listOf(
                "Color dominante del póster",
                "Cantidad de letras del título",
                "Orden alfabético del género",
                "Popularidad del nombre"
            ),
            feedback = "La duración es el dato útil para estimar tiempo de visualización."
        )
    }

    private fun buildTitleLengthQuestion(
        anime: Anime,
        difficulty: TriviaDifficulty
    ): TriviaQuestion {
        val words = anime.title
            .split(Regex("\\s+"))
            .count { it.isNotBlank() }

        val correct = when {
            words <= 1 -> "Título de una palabra"
            words <= 3 -> "Título corto"
            words <= 6 -> "Título medio"
            else -> "Título largo"
        }

        return buildQuestion(
            id = "${anime.id}_personality_title_length_${difficulty.name.lowercase()}",
            anime = anime,
            difficulty = difficulty,
            question = when (difficulty) {
                TriviaDifficulty.EASY ->
                    "Por nombre en pantalla, ¿cómo se siente el título ${anime.title}?"

                TriviaDifficulty.MEDIUM ->
                    "Branding anime: por cantidad de palabras, ¿qué tipo de título tiene ${anime.title}?"

                TriviaDifficulty.HARD ->
                    "Análisis de nombre: ¿cómo se clasifica el título ${anime.title} por longitud?"
            },
            correctOption = correct,
            distractors = listOf(
                "Título de una palabra",
                "Título corto",
                "Título medio",
                "Título largo"
            ).filterNot { it == correct },
            feedback = "La clasificación se basa en la cantidad de palabras del título registrado."
        )
    }

    private fun buildDataIntegrityQuestion(anime: Anime): TriviaQuestion {
        return buildQuestion(
            id = "${anime.id}_personality_data_integrity",
            anime = anime,
            difficulty = TriviaDifficulty.HARD,
            question = "Pregunta anti-relleno: si AnimeDev no tiene un dato confirmado de ${anime.title}, ¿qué debería hacer?",
            correctOption = "Evitar esa pregunta o marcar el dato como no disponible",
            distractors = listOf(
                "Inventar un valor promedio",
                "Copiar datos de otro anime parecido",
                "Asumir que siempre son 12 episodios",
                "Usar cualquier escena popular de internet"
            ),
            feedback = "Correcto. Mejor menos preguntas, pero confiables. Esa decisión salva la calidad del producto."
        )
    }

    private fun buildQuestion(
        id: String,
        anime: Anime,
        difficulty: TriviaDifficulty,
        question: String,
        correctOption: String,
        distractors: List<String>,
        feedback: String
    ): TriviaQuestion {
        val options = buildOptions(correctOption, distractors)

        return TriviaQuestion(
            id = id,
            animeId = anime.id,
            difficulty = difficulty,
            question = question,
            options = options,
            correctAnswerIndex = options.indexOf(correctOption),
            feedback = feedback
        )
    }

    private fun buildOptions(
        correctOption: String,
        distractors: List<String>
    ): List<String> {
        val values = linkedSetOf<String>()
        values += correctOption

        distractors
            .filter { it.isNotBlank() }
            .filterNot { it.equals(correctOption, ignoreCase = true) }
            .forEach { values += it }

        val fallbackDistractors = listOf(
            "Dato no registrado",
            "No corresponde a la ficha",
            "Información no disponible",
            "Otra opción"
        )

        fallbackDistractors
            .filterNot { it.equals(correctOption, ignoreCase = true) }
            .forEach {
                if (values.size < 4) {
                    values += it
                }
            }

        return values.take(4).shuffled()
    }

    private fun numericDistractors(
        correct: Int,
        candidates: List<Int>,
        minValue: Int
    ): List<Int> {
        val values = linkedSetOf<Int>()

        candidates
            .map { it.coerceAtLeast(minValue) }
            .filterNot { it == correct }
            .forEach { values += it }

        var fallback = correct + 1
        while (values.size < 3) {
            val candidate = fallback.coerceAtLeast(minValue)
            if (candidate != correct) {
                values += candidate
            }
            fallback++
        }

        return values.take(3)
    }

    private fun episodeDistractorStep(totalEpisodes: Int): Int = when {
        totalEpisodes >= 500 -> 100
        totalEpisodes >= 200 -> 50
        totalEpisodes >= 100 -> 25
        totalEpisodes >= 27 -> 12
        totalEpisodes >= 14 -> 6
        else -> 1
    }

    private fun extractSynopsisKeywords(synopsis: String): List<String> {
        if (synopsis.isBlank()) return emptyList()

        return Regex("[A-Za-zÁÉÍÓÚÜÑáéíóúüñ0-9]+")
            .findAll(synopsis)
            .map { it.value.trim() }
            .filter { it.length >= 5 }
            .filterNot { stopWords.contains(it.lowercase()) }
            .distinctBy { it.lowercase() }
            .take(8)
            .toList()
    }

    private fun List<String>.filterNotIn(values: List<String>): List<String> {
        return filter { candidate ->
            values.none { it.equals(candidate, ignoreCase = true) }
        }
    }

    private fun DurationType.toReadableText(): String = when (this) {
        DurationType.SHORT -> "Corto (≤15 min)"
        DurationType.MEDIUM -> "Medio (16-35 min)"
        DurationType.LONG -> "Largo (36+ min)"
    }

    private fun DurationType.toAverageMinutes(): Int = when (this) {
        DurationType.SHORT -> 12
        DurationType.MEDIUM -> 24
        DurationType.LONG -> 45
    }

    private fun EmissionStatus.toReadableText(): String = when (this) {
        EmissionStatus.ON_AIR -> "En emisión"
        EmissionStatus.FINISHED -> "Finalizado"
        EmissionStatus.ON_BREAK -> "En pausa"
    }
}