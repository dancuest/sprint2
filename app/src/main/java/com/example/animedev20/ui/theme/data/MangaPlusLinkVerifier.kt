package com.example.animedev20.ui.theme.data

import com.example.animedev20.ui.theme.domain.model.Anime
import java.net.HttpURLConnection
import java.net.URL
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
        if (!anime.mangaPlusUrl.startsWith("https://mangaplus.shueisha.co.jp/titles/")) {
            return MangaPlusLinkIssue(
                animeId = anime.id,
                animeTitle = anime.title,
                url = anime.mangaPlusUrl,
                reason = "La URL no pertenece al dominio oficial de Manga Plus."
            )
        }
        return runCatching {
            val connection = URL(anime.mangaPlusUrl).openConnection() as HttpURLConnection
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.setRequestProperty("User-Agent", "AnimeDev/1.0")
            val html = connection.inputStream.bufferedReader().use { it.readText() }
            val normalizedHtml = html.lowercase(Locale.getDefault())
            val expectedTitles = listOfNotNull(anime.title, anime.originalTitle)
                .map { it.lowercase(Locale.getDefault()) }
            val matches = expectedTitles.any { normalizedHtml.contains(it) }
            if (matches) {
                null
            } else {
                MangaPlusLinkIssue(
                    animeId = anime.id,
                    animeTitle = anime.title,
                    url = anime.mangaPlusUrl,
                    reason = "El título del anime no coincide con el contenido detectado en la página."
                )
            }
        }.getOrElse { error ->
            MangaPlusLinkIssue(
                animeId = anime.id,
                animeTitle = anime.title,
                url = anime.mangaPlusUrl,
                reason = "No se pudo verificar la URL (${error.message})."
            )
        }
    }
}
