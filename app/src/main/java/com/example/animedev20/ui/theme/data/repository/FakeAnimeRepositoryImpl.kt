package com.example.animedev20.ui.theme.data.repository

import com.example.animedev20.ui.theme.data.FakeDataSource
import com.example.animedev20.ui.theme.domain.model.Anime
import com.example.animedev20.ui.theme.domain.model.AnimeDetail
import com.example.animedev20.ui.theme.domain.model.Genre
import com.example.animedev20.ui.theme.domain.repository.AnimeRepository
import kotlinx.coroutines.delay

class FakeAnimeRepositoryImpl : AnimeRepository {

    override suspend fun getHeroRecommendation(): Anime {
        delay(600)
        return FakeDataSource.heroAnime
    }

    override suspend fun getAnimesByGenre(genreId: String): List<Anime> {
        delay(400)
        return FakeDataSource.animeCatalog.filter { anime ->
            anime.genres.any { it.id == genreId }
        }
    }
    override suspend fun getAnimeDetail(animeId: Long): AnimeDetail {
        delay(500)
        return FakeDataSource.getAnimeDetail(animeId)
    }

    override suspend fun searchAnime(query: String): List<Anime> {
        delay(400)
        return FakeDataSource.animeCatalog.filter { anime ->
            val normalizedQuery = query.trim().lowercase()
            anime.title.lowercase().contains(normalizedQuery) ||
                anime.synopsis.lowercase().contains(normalizedQuery) ||
                (anime.originalTitle?.lowercase()?.contains(normalizedQuery) ?: false)
        }
    }

    override suspend fun getGenres(): List<Genre> {
        delay(300)
        return FakeDataSource.genres
    }
}
