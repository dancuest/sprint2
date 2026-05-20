package com.example.animedev20.ui.theme.domain.usecase

import com.example.animedev20.ui.theme.domain.model.Anime
import com.example.animedev20.ui.theme.domain.model.AnimeSection
import com.example.animedev20.ui.theme.domain.model.Genre
import com.example.animedev20.ui.theme.domain.model.HomeContent
import com.example.animedev20.ui.theme.domain.repository.AnimeRepository
import com.example.animedev20.ui.theme.domain.repository.UserRepository
import kotlinx.coroutines.delay

class GetHomeContentUseCase(
    private val animeRepository: AnimeRepository,
    private val userRepository: UserRepository
) {

    private companion object {
        private const val THROTTLE_MS = 500L
        private const val RETRY_DELAY_MS = 1_200L
        private const val MAX_ATTEMPTS_PER_GENRE = 1
        private const val TARGET_SECTION_SIZE = 10
    }

    suspend operator fun invoke(): Result<HomeContent> = try {
        val preferredGenres = runCatching {
            userRepository.getPreferredGenres()
        }.getOrElse {
            emptyList()
        }.distinctBy { it.id }

        val adaptiveRecommendations = runCatching {
            animeRepository.getAdaptiveRecommendations()
        }.getOrElse {
            emptyList()
        }.distinctBy { it.id }

        val heroCandidate = adaptiveRecommendations.firstOrNull()
            ?: resolveHeroAnime(preferredGenres)
            ?: return Result.failure(
                Exception("No se pudo cargar el contenido principal. Verifica tu conexión.")
            )

        /**
         * El hero del Home debe mostrarse siempre con sinopsis en español.
         *
         * El candidato puede venir desde recomendaciones adaptativas o desde género,
         * endpoints que pueden traer datos resumidos. Por eso se hidrata con /detail,
         * que en backend devuelve la sinopsis traducida.
         */
        val heroAnime = hydrateHeroAnime(heroCandidate)

        val recommendationList = resolveRecommendationList(
            heroAnime = heroAnime,
            adaptiveRecommendations = adaptiveRecommendations,
            preferredGenres = preferredGenres
        )

        val usedAnimeIds = linkedSetOf<Long>()
        usedAnimeIds += heroAnime.id

        val sections = mutableListOf<AnimeSection>()

        if (recommendationList.isNotEmpty()) {
            usedAnimeIds += recommendationList.map { it.id }
            sections += AnimeSection(
                genre = Genre("recommendations", "Para Ti"),
                animes = recommendationList,
                source = "adaptive"
            )
        }

        for (genre in preferredGenres) {
            val candidates = loadGenreCandidates(genre.id)

            val uniqueItems = candidates
                .distinctBy { it.id }
                .filterNot { it.id in usedAnimeIds }
                .take(TARGET_SECTION_SIZE)

            if (uniqueItems.isNotEmpty()) {
                usedAnimeIds += uniqueItems.map { it.id }
                sections += AnimeSection(
                    genre = genre,
                    animes = uniqueItems
                )
            }
        }

        Result.success(
            HomeContent(
                heroAnime = heroAnime,
                preferredGenres = preferredGenres,
                sections = sections
            )
        )
    } catch (e: Exception) {
        Result.failure(e)
    }

    private suspend fun resolveRecommendationList(
        heroAnime: Anime,
        adaptiveRecommendations: List<Anime>,
        preferredGenres: List<Genre>
    ): List<Anime> {
        val uniqueRecommendations = linkedMapOf<Long, Anime>()

        // 1) Prioridad absoluta: resultados adaptativos del backend.
        adaptiveRecommendations
            .asSequence()
            .filterNot { it.id == heroAnime.id }
            .forEach { anime ->
                if (uniqueRecommendations.size < TARGET_SECTION_SIZE) {
                    uniqueRecommendations.putIfAbsent(anime.id, anime)
                }
            }

        if (uniqueRecommendations.size >= TARGET_SECTION_SIZE) {
            return uniqueRecommendations.values.toList()
        }

        // 2) Relleno controlado SOLO con géneros preferidos del usuario.
        val preferredGenreIds = preferredGenres.map { it.id }.distinct()

        for (genreId in preferredGenreIds) {
            val candidates = loadGenreCandidates(genreId)

            for (anime in candidates) {
                if (anime.id == heroAnime.id) continue
                uniqueRecommendations.putIfAbsent(anime.id, anime)

                if (uniqueRecommendations.size >= TARGET_SECTION_SIZE) {
                    return uniqueRecommendations.values.toList()
                }
            }
        }

        return uniqueRecommendations.values.toList()
    }

    private suspend fun loadGenreCandidates(genreId: String): List<Anime> {
        repeat(MAX_ATTEMPTS_PER_GENRE) { attempt ->
            delay(THROTTLE_MS)

            val items = runCatching {
                animeRepository.getAnimesByGenre(genreId)
            }.getOrElse {
                emptyList()
            }

            if (items.isNotEmpty()) {
                return items
            }

            if (attempt < MAX_ATTEMPTS_PER_GENRE - 1) {
                delay(RETRY_DELAY_MS)
            }
        }

        return emptyList()
    }

    /**
     * Resuelve el anime hero con fallback progresivo:
     * 1. Primer anime del primer género preferido del usuario
     * 2. Hero general del backend como último recurso
     */
    private suspend fun resolveHeroAnime(preferredGenres: List<Genre>): Anime? {
        if (preferredGenres.isNotEmpty()) {
            runCatching {
                animeRepository.getAnimesByGenre(preferredGenres.first().id).firstOrNull()
            }.onSuccess { anime ->
                if (anime != null) {
                    return anime
                }
            }
        }

        return runCatching {
            animeRepository.getHeroRecommendation()
        }.getOrNull()
    }

    private suspend fun hydrateHeroAnime(anime: Anime): Anime {
        return runCatching {
            animeRepository.getAnimeDetail(anime.id).anime
        }.getOrElse {
            anime
        }
    }
}