package com.example.animedev20.ui.theme.domain.usecase

import com.example.animedev20.ui.theme.domain.model.AnimeSection
import com.example.animedev20.ui.theme.domain.model.HomeContent
import com.example.animedev20.ui.theme.domain.repository.AnimeRepository
import com.example.animedev20.ui.theme.domain.repository.UserRepository
import kotlinx.coroutines.supervisorScope

class GetHomeContentUseCase(
    private val animeRepository: AnimeRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): Result<HomeContent> = runCatching {
        supervisorScope {
            // 1) Primero el hero (una sola llamada)
            val heroAnime = animeRepository.getHeroRecommendation()

            // 2) Obtiene TODOS los géneros preferidos del usuario
            val preferredGenres = userRepository.getPreferredGenres()

            // 3) Carga SECUENCIAL por género seleccionado.
            //    Si una sección falla, NO tumba el Home: la dejamos vacía.
            val sections = preferredGenres.map { genre ->
                runCatching {
                    val animes = animeRepository.getAnimesByGenre(genre.id)
                    AnimeSection(genre = genre, animes = animes)
                }.getOrElse {
                    AnimeSection(genre = genre, animes = emptyList())
                }
            }

            HomeContent(
                heroAnime = heroAnime,
                preferredGenres = preferredGenres,
                sections = sections
            )
        }
    }
}
