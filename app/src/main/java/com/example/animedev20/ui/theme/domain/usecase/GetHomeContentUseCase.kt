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
        private const val THROTTLE_MS = 250L
        private const val TARGET_SECTION_SIZE = 10
        private const val MAX_REPEAT_PER_SECTION = 2
        // ID del género "Action" en Jikan como fallback para el hero
        private const val FALLBACK_GENRE_ID = "1"
    }

    suspend operator fun invoke(): Result<HomeContent> = try {

        // Preferencias del usuario (nunca falla — tiene fallback a lista vacía)
        val preferredGenres = runCatching {
            userRepository.getPreferredGenres()
        }.getOrElse { emptyList() }

        // HERO: intenta recomendación adaptativa, si falla busca fallback progresivo
        val heroAnime = resolveHeroAnime(preferredGenres)
            ?: return Result.failure(
                Exception("No se pudo cargar el contenido principal. Verifica tu conexión.")
            )

        val usedAnimeIds = linkedSetOf<Long>()
        usedAnimeIds += heroAnime.id

        val sections = mutableListOf<AnimeSection>()

        // RECOMENDACIONES ADAPTATIVAS
        val recommendationCandidates = runCatching {
            animeRepository.getAdaptiveRecommendations()
        }.getOrElse { emptyList() }

        val recommendationList = recommendationCandidates
            .filterNot { it.id in usedAnimeIds }
            .take(TARGET_SECTION_SIZE)

        if (recommendationList.isNotEmpty()) {
            usedAnimeIds += recommendationList.map { it.id }
            sections += AnimeSection(
                genre = Genre("recommendations", "Para Ti"),
                animes = recommendationList
            )
        }

        // SECCIONES POR GÉNEROS PREFERIDOS
        for ((index, genre) in preferredGenres.withIndex()) {

            val candidates = runCatching {
                animeRepository.getAnimesByGenre(genre.id)
            }.getOrElse { emptyList() }

            val uniqueItems = candidates
                .filterNot { it.id in usedAnimeIds }
                .take(TARGET_SECTION_SIZE)
                .toMutableList()

            if (uniqueItems.size < TARGET_SECTION_SIZE) {
                val repeats = candidates
                    .filterNot { anime -> uniqueItems.any { it.id == anime.id } }
                    .take(MAX_REPEAT_PER_SECTION)
                uniqueItems += repeats
            }

            if (uniqueItems.isNotEmpty()) {
                usedAnimeIds += uniqueItems.map { it.id }
                sections += AnimeSection(
                    genre = genre,
                    animes = uniqueItems
                )
            }

            if (index != preferredGenres.lastIndex) {
                delay(THROTTLE_MS)
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

    /**
     * Resuelve el anime hero con fallback progresivo:
     * 1. Recomendación adaptativa del backend
     * 2. Primer anime del primer género preferido del usuario
     * 3. Primer anime del género Action (id=1) como último recurso
     * Retorna null solo si los tres fallan (sin red).
     */
    private suspend fun resolveHeroAnime(preferredGenres: List<Genre>): Anime? {
        // Intento 1: recomendación adaptativa
        runCatching {
            animeRepository.getHeroRecommendation()
        }.onSuccess { hero ->
            if (hero != null) return hero
        }

        // Intento 2: primer anime del primer género preferido
        if (preferredGenres.isNotEmpty()) {
            runCatching {
                animeRepository.getAnimesByGenre(preferredGenres.first().id).firstOrNull()
            }.onSuccess { anime ->
                if (anime != null) return anime
            }
        }

        // Intento 3: género Action como fallback final
        return runCatching {
            animeRepository.getAnimesByGenre(FALLBACK_GENRE_ID).firstOrNull()
        }.getOrNull()
    }
}
