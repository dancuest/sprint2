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
        private const val MAX_ATTEMPTS_PER_GENRE = 2
        private const val TARGET_SECTION_SIZE = 10

        // ID del género "Action" en Jikan como fallback para el hero
        private const val FALLBACK_GENRE_ID = "1"
    }

    suspend operator fun invoke(): Result<HomeContent> = try {
        val preferredGenres = runCatching {
            userRepository.getPreferredGenres()
        }.getOrElse {
            emptyList()
        }.distinctBy { it.id }

        val heroAnime = resolveHeroAnime(preferredGenres)
            ?: return Result.failure(
                Exception("No se pudo cargar el contenido principal. Verifica tu conexión.")
            )

        val usedAnimeIds = mutableSetOf<Long>()
        usedAnimeIds.add(heroAnime.id)

        val sections = mutableListOf<AnimeSection>()

        val recommendationCandidates = runCatching {
            animeRepository.getAdaptiveRecommendations()
        }.getOrElse {
            emptyList()
        }

        val recommendationList = recommendationCandidates
            .filterNot { it.id in usedAnimeIds }
            .take(TARGET_SECTION_SIZE)

        if (recommendationList.isNotEmpty()) {
            usedAnimeIds.addAll(recommendationList.map { it.id })
            sections += AnimeSection(
                genre = Genre("recommendations", "Para Ti"),
                animes = recommendationList
            )
        }

        for (genre in preferredGenres) {
            val candidates = loadGenreCandidates(genre.id)

            val uniqueItems = candidates
                .distinctBy { it.id }
                .filterNot { it.id in usedAnimeIds }
                .take(TARGET_SECTION_SIZE)

            if (uniqueItems.isNotEmpty()) {
                usedAnimeIds.addAll(uniqueItems.map { it.id })
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
     * 1. Recomendación adaptativa del backend
     * 2. Primer anime del primer género preferido del usuario
     * 3. Primer anime del género Action (id=1) como último recurso
     */
    private suspend fun resolveHeroAnime(preferredGenres: List<Genre>): Anime? {
        runCatching {
            animeRepository.getHeroRecommendation()
        }.onSuccess { hero ->
            if (hero != null) {
                return hero
            }
        }

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
            animeRepository.getAnimesByGenre(FALLBACK_GENRE_ID).firstOrNull()
        }.getOrNull()
    }
}