package com.example.animedev20.ui.theme.domain.usecase

import com.example.animedev20.ui.theme.domain.model.AnimeSection
import com.example.animedev20.ui.theme.domain.model.HomeContent
import com.example.animedev20.ui.theme.domain.repository.AnimeRepository
import com.example.animedev20.ui.theme.domain.repository.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope

class GetHomeContentUseCase(
    private val animeRepository: AnimeRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<HomeContent> = try {
        supervisorScope {
            // 1) Primero el hero y los géneros preferidos en paralelo
            val heroDeferred = async { animeRepository.getHeroRecommendation() }
            val genresDeferred = async { userRepository.getPreferredGenres() }

            val heroAnime = heroDeferred.await()
            val preferredGenres = genresDeferred.await()

            // 2) Carga CONCURRENTE por cada género seleccionado.
            //    Esto permite que aparezcan todos los que el usuario haya elegido sin esperas excesivas.
            val sectionTasks = preferredGenres.map { genre ->
                async {
                    try {
                        val animes = animeRepository.getAnimesByGenre(genre.id)
                        AnimeSection(genre = genre, animes = animes)
                    } catch (e: Exception) {
                        AnimeSection(genre = genre, animes = emptyList())
                    }
                }
            }

            val sections = sectionTasks.awaitAll()

            Result.success(
                HomeContent(
                    heroAnime = heroAnime,
                    preferredGenres = preferredGenres,
                    sections = sections
                )
            )
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
