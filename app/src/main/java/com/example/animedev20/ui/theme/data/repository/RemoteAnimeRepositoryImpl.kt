package com.example.animedev20.ui.theme.data.repository

import com.example.animedev20.ui.theme.data.remote.AnimeApi
import com.example.animedev20.ui.theme.domain.model.Anime
import com.example.animedev20.ui.theme.domain.model.AnimeDetail
import com.example.animedev20.ui.theme.domain.model.Genre
import com.example.animedev20.ui.theme.domain.repository.AnimeRepository
import retrofit2.HttpException

class RemoteAnimeRepositoryImpl(
    private val animeApi: AnimeApi
) : AnimeRepository {

    override suspend fun getHeroRecommendation(): Anime {
        val fallback = suspend {
            val response = animeApi.getTop(limit = 1)
            response.data.firstOrNull() ?: throw Exception("No se encontró un anime destacado.")
        }

        return fetchWithFallback(
            primary = { animeApi.getHero().data },
            fallback = fallback,
            errorMessage = "No fue posible cargar el anime destacado.\nIntenta más tarde."
        )
    }

    override suspend fun getAnimesByGenre(genreId: String): List<Anime> {
        val fallback = suspend { animeApi.getTop(limit = 10).data }

        return fetchWithFallback(
            primary = { animeApi.getByGenre(genreId = genreId, limit = 10).data },
            fallback = fallback,
            errorMessage = "No fue posible cargar los animes del género solicitado."
        )
    }

    override suspend fun getAnimeDetail(animeId: Long): AnimeDetail {
        val fallback = suspend {
            val anime = animeApi.getById(animeId).data
            AnimeDetail(anime = anime, culturalNotes = emptyList(), trailers = emptyList())
        }

        return fetchWithFallback(
            primary = { animeApi.getDetail(animeId).data },
            fallback = fallback,
            errorMessage = "No fue posible cargar el detalle del anime."
        )
    }

    override suspend fun searchAnime(query: String): List<Anime> {
        return safeCall(
            call = { animeApi.search(q = query, limit = 10).data },
            errorMessage = "No fue posible realizar la búsqueda en este momento."
        )
    }

    override suspend fun getGenres(): List<Genre> {
        return safeCall(
            call = { animeApi.getGenres().data },
            errorMessage = "No fue posible cargar la lista de géneros."
        )
    }

    private suspend fun <T> fetchWithFallback(
        primary: suspend () -> T,
        fallback: suspend () -> T,
        errorMessage: String
    ): T {
        return try {
            primary()
        } catch (error: HttpException) {
            // Fallback también para rate-limit / gateway / server errors
            when (error.code()) {
                404, 408, 429, 500, 502, 503, 504 -> safeCall(fallback, errorMessage)
                else -> throw Exception(errorMessage)
            }
        } catch (error: Exception) {
            // Si algo random truena (timeouts, etc), intenta fallback
            safeCall(fallback, errorMessage)
        }
    }

    private suspend fun <T> safeCall(
        call: suspend () -> T,
        errorMessage: String
    ): T {
        return try {
            call()
        } catch (error: Exception) {
            throw Exception(errorMessage)
        }
    }
}
