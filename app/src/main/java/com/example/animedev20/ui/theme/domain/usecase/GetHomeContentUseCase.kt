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
    }

    suspend operator fun invoke(): Result<HomeContent> = try {
        val heroAnime = animeRepository.getHeroRecommendation()
        val preferredGenres = userRepository.getPreferredGenres()

        val usedAnimeIds = linkedSetOf<Long>()
        usedAnimeIds += heroAnime.id

        val sections = mutableListOf<AnimeSection>()

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
        } else {
            sections += AnimeSection(
                genre = Genre("recommendations", "Para Ti"),
                animes = emptyList()
            )
        }

        for ((index, genre) in preferredGenres.withIndex()) {
            val candidates = try {
                animeRepository.getAnimesByGenre(genre.id)
            } catch (_: Exception) {
                emptyList()
            }

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
}