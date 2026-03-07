package com.example.animedev20.ui.theme.domain.usecase

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
        private const val THROTTLE_MS = 400L
    }

    suspend operator fun invoke(): Result<HomeContent> = try {
        val heroAnime = animeRepository.getHeroRecommendation()
        val preferredGenres = userRepository.getPreferredGenres()
        val sections = mutableListOf<AnimeSection>()

        val recommendations = runCatching { animeRepository.getAdaptiveRecommendations() }
            .getOrElse { emptyList() }

        sections += AnimeSection(
            genre = Genre("recommendations", "Para Ti (Sugerencias Inteligentes)"),
            animes = recommendations
        )

        if (preferredGenres.isNotEmpty()) delay(THROTTLE_MS)

        for ((index, genre) in preferredGenres.withIndex()) {
            val animes = try {
                animeRepository.getAnimesByGenre(genre.id)
            } catch (_: Exception) {
                emptyList()
            }

            sections += AnimeSection(
                genre = genre,
                animes = animes
            )

            if (index != preferredGenres.lastIndex) delay(THROTTLE_MS)
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
