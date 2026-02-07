package com.example.animedev20.ui.theme.data

import com.example.animedev20.ui.theme.domain.model.Anime
import java.net.URLEncoder
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class MangaPlusLinkIssue(
    val animeId: Long,
    val animeTitle: String,
    val url: String,
    val reason: String
)

object MangaPlusLinkVerifier {
    suspend fun verifyAll(animes: List<Anime>): List<MangaPlusLinkIssue> = withContext(Dispatchers.IO) {
        animes.mapNotNull { anime ->
            verifyAnimeLink(anime)
        }
    }

    private fun verifyAnimeLink(anime: Anime): MangaPlusLinkIssue? {
        if (!anime.mangaPlusUrl.startsWith("https://mangaplus.shueisha.co.jp/titles")) {
            return MangaPlusLinkIssue(
                animeId = anime.id,
                animeTitle = anime.title,
                url = anime.mangaPlusUrl,
                reason = "La URL no pertenece al dominio oficial de Manga Plus."
            )
        }
        val normalizedUrl = anime.mangaPlusUrl.lowercase(Locale.getDefault())
        val expectedTitles = listOfNotNull(anime.title, anime.originalTitle)
            .map { it.lowercase(Locale.getDefault()) }
        val matches = expectedTitles.any { title ->
            val encodedTitle = URLEncoder.encode(title, "UTF-8")
            normalizedUrl.contains(encodedTitle)
        }
        if (matches) {
            return null
        }
        return MangaPlusLinkIssue(
            animeId = anime.id,
            animeTitle = anime.title,
            url = anime.mangaPlusUrl,
            reason = "El enlace de búsqueda no incluye el título esperado del anime."
        )
    }
}
