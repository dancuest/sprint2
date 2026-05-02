package com.example.animedev20.ui.theme.data.repository

import android.util.Log
import com.example.animedev20.ui.theme.data.remote.AnimeApi
import com.example.animedev20.ui.theme.domain.model.Anime
import com.example.animedev20.ui.theme.domain.model.AnimeDetail
import com.example.animedev20.ui.theme.domain.model.Genre
import com.example.animedev20.ui.theme.domain.repository.AnimeRepository
import retrofit2.HttpException

class RemoteAnimeRepositoryImpl(
    private val animeApi: AnimeApi
) : AnimeRepository {

    companion object {
        private const val TAG = "RemoteAnimeRepository"
    }

    override suspend fun getHeroRecommendation(): Anime {
        val fallback = suspend {
            val response = animeApi.getTop(limit = 1)
            val topAnime = response.data.firstOrNull()
                ?: throw Exception("No se encontró un anime destacado.")

            try {
                animeApi.getDetail(topAnime.id).data.anime
            } catch (error: Exception) {
                topAnime
            }
        }

        return fetchWithFallback(
            primary = { animeApi.getHero().data },
            fallback = fallback,
            errorMessage = "No fue posible cargar el anime destacado."
        )
    }

    override suspend fun getAnimesByGenre(genreId: String): List<Anime> {
        return try {
            animeApi.getByGenre(genreId = genreId, limit = 18).data
        } catch (error: HttpException) {
            Log.w(TAG, "Genre request failed for genreId=$genreId code=${error.code()}")
            emptyList()
        } catch (error: Exception) {
            Log.w(TAG, "Genre request failed for genreId=$genreId", error)
            emptyList()
        }
    }

    override suspend fun getAnimeDetail(animeId: Long): AnimeDetail {
        val fallback = suspend {
            val anime = animeApi.getById(animeId).data
            AnimeDetail(
                anime = anime,
                culturalNotes = emptyList(),
                trailers = emptyList()
            )
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

    override suspend fun getAdaptiveRecommendations(): List<Anime> {
        return try {
            animeApi.getAdaptiveRecommendations().data
        } catch (error: HttpException) {
            Log.w(
                TAG,
                "Adaptive recommendations failed code=${error.code()}, returning empty list for explicit fallback handling"
            )
            emptyList()
        } catch (error: Exception) {
            Log.w(
                TAG,
                "Adaptive recommendations failed, returning empty list for explicit fallback handling",
                error
            )
            emptyList()
        }
    }

    private suspend fun <T> fetchWithFallback(
        primary: suspend () -> T,
        fallback: suspend () -> T,
        errorMessage: String
    ): T {
        return try {
            primary()
        } catch (error: HttpException) {
            when (error.code()) {
                404, 408, 429, 500, 502, 503, 504 -> safeCall(fallback, errorMessage)
                else -> throw Exception(errorMessage)
            }
        } catch (error: Exception) {
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