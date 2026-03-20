package com.example.animedev20.ui.theme.data.repository

import android.content.Context
import androidx.core.content.edit
import com.example.animedev20.ui.theme.domain.model.Anime
import com.example.animedev20.ui.theme.domain.model.DurationType
import com.example.animedev20.ui.theme.domain.model.EmissionStatus
import com.example.animedev20.ui.theme.domain.model.Genre
import com.example.animedev20.ui.theme.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

object FakeFavoritesRepositoryImpl : FavoritesRepository {

    private const val PREFS_NAME = "animedev_favorites_prefs"
    private const val KEY_FAVORITES = "favorite_animes"

    private val favoriteAnimes = MutableStateFlow<List<Anime>>(emptyList())
    private var appContext: Context? = null
    private var isInitialized = false

    fun initialize(context: Context) {
        if (isInitialized) return
        appContext = context.applicationContext
        val prefs = appContext!!.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val restored = prefs.getStringSet(KEY_FAVORITES, emptySet())
            ?.mapNotNull(::decodeAnime)
            .orEmpty()
        favoriteAnimes.value = restored
        isInitialized = true
    }

    override val favorites: Flow<List<Anime>> = favoriteAnimes.asStateFlow()

    override suspend fun addFavorite(anime: Anime) {
        val current = favoriteAnimes.value
        if (current.any { it.id == anime.id }) return
        favoriteAnimes.value = current + anime
        persistFavorites()
    }

    override suspend fun removeFavorite(animeId: Long) {
        val current = favoriteAnimes.value
        favoriteAnimes.value = current.filterNot { it.id == animeId }
        persistFavorites()
    }

    override suspend fun toggleFavorite(anime: Anime) {
        if (favoriteAnimes.value.any { it.id == anime.id }) {
            removeFavorite(anime.id)
        } else {
            addFavorite(anime)
        }
    }

    override fun isFavorite(animeId: Long): Flow<Boolean> =
        favoriteAnimes
            .map { list -> list.any { it.id == animeId } }
            .distinctUntilChanged()

    private fun persistFavorites() {
        val context = appContext ?: return
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
            putStringSet(KEY_FAVORITES, favoriteAnimes.value.map { encodeAnime(it) }.toSet())
        }
    }

    private fun encodeAnime(anime: Anime): String {
        val genres = anime.genres.joinToString("\u001E") { "${it.id}\u001F${it.name}" }
        return listOf(
            anime.id.toString(),
            anime.externalApiId,
            anime.title,
            anime.originalTitle.orEmpty(),
            anime.synopsis,
            anime.coverImageUrl,
            anime.mangaPlusUrl,
            anime.totalEpisodes?.toString().orEmpty(),
            anime.durationType.name,
            anime.emissionStatus.name,
            anime.releaseYear?.toString().orEmpty(),
            genres
        ).joinToString("\u001D")
    }

    private fun decodeAnime(value: String): Anime? {
        val parts = value.split("\u001D")
        if (parts.size < 12) return null
        val genres = if (parts[11].isBlank()) {
            emptyList()
        } else {
            parts[11].split("\u001E").mapNotNull { encodedGenre ->
                val genreParts = encodedGenre.split("\u001F")
                if (genreParts.size != 2) null else Genre(id = genreParts[0], name = genreParts[1])
            }
        }
        return Anime(
            id = parts[0].toLongOrNull() ?: return null,
            externalApiId = parts[1],
            title = parts[2],
            originalTitle = parts[3].ifBlank { null },
            synopsis = parts[4],
            coverImageUrl = parts[5],
            mangaPlusUrl = parts[6],
            totalEpisodes = parts[7].toIntOrNull(),
            durationType = DurationType.valueOf(parts[8]),
            emissionStatus = EmissionStatus.valueOf(parts[9]),
            releaseYear = parts[10].toIntOrNull(),
            genres = genres
        )
    }
}
