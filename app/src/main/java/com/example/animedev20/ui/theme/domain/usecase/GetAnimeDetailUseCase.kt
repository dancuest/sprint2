package com.example.animedev20.ui.theme.domain.usecase

import com.example.animedev20.ui.theme.domain.model.AnimeDetail
import com.example.animedev20.ui.theme.domain.repository.AnimeRepository

class GetAnimeDetailUseCase(
                            private val animeRepository: AnimeRepository
) {
    suspend operator fun invoke(animeId: Long): Result<AnimeDetail> =
    runCatching { animeRepository.getAnimeDetail(animeId) }
}