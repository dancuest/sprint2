package com.example.animedev20.ui.theme.domain.usecase

import com.example.animedev20.ui.theme.domain.model.AnimeSection
import com.example.animedev20.ui.theme.domain.model.HomeContent
import com.example.animedev20.ui.theme.domain.repository.AnimeRepository
import com.example.animedev20.ui.theme.domain.repository.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class GetHomeContentUseCase(
    private val animeRepository: AnimeRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<HomeContent> = runCatching {
        coroutineScope {
            val heroAnimeDeferred = async { animeRepository.getHeroRecommendation() }

            val preferredGenres = userRepository.getPreferredGenres()

            val sectionsDeferred = preferredGenres.map { genre ->
                async {
                    val animes = animeRepository.getAnimesByGenre(genre.id)
                    AnimeSection(genre = genre, animes = animes)
                }
            }

            HomeContent(
                heroAnime = heroAnimeDeferred.await(),
                sections = sectionsDeferred.map { it.await() }
            )
        }
    }
}