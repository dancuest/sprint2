package com.example.animedev20.ui.theme.data

import com.example.animedev20.ui.theme.domain.model.Anime
import com.example.animedev20.ui.theme.domain.model.AnimeDetail
import com.example.animedev20.ui.theme.domain.model.AnimeSection
import com.example.animedev20.ui.theme.domain.model.DurationType
import com.example.animedev20.ui.theme.domain.model.EmissionStatus
import com.example.animedev20.ui.theme.domain.model.Episode
import com.example.animedev20.ui.theme.domain.model.Genre
import com.example.animedev20.ui.theme.domain.model.TriviaProfileStats
import com.example.animedev20.ui.theme.domain.model.UserProfile
import com.example.animedev20.ui.theme.domain.model.UserSettings
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaDifficulty

object FakeDataSource {
    val shonen = Genre(id = "1", name = "Shonen")
    val seinen = Genre(id = "2", name = "Seinen")
    val aventura = Genre(id = "3", name = "Aventura")
    val misterio = Genre(id = "4", name = "Misterio")
    val drama = Genre(id = "5", name = "Drama")

    val genres = listOf(shonen, seinen, aventura, misterio, drama)

    val animeCatalog = listOf(
        Anime(
            id = 1,
            externalApiId = "kimetsu_no_yaiba",
            title = "Demon Slayer",
            originalTitle = "Kimetsu no Yaiba",
            synopsis = "Tanjiro se convierte en cazador de demonios para salvar a su hermana y vengar a su familia.",
            coverImageUrl = "https://cdn.myanimelist.net/images/anime/1286/99889.jpg",
            totalEpisodes = 26,
            durationType = DurationType.MEDIUM,
            emissionStatus = EmissionStatus.ON_AIR,
            releaseYear = 2019,
            genres = listOf(shonen, aventura)
        ),
        Anime(
            id = 2,
            externalApiId = "vinland_saga",
            title = "Vinland Saga",
            originalTitle = "Vinland Saga",
            synopsis = "Thorfinn busca venganza en una historia épica sobre exploración y honor vikingo.",
            coverImageUrl = "https://cdn.myanimelist.net/images/anime/1907/117414.jpg",
            totalEpisodes = 48,
            durationType = DurationType.LONG,
            emissionStatus = EmissionStatus.ON_AIR,
            releaseYear = 2019,
            genres = listOf(seinen, aventura, drama)
        ),
        Anime(
            id = 3,
            externalApiId = "made_in_abyss",
            title = "Made in Abyss",
            originalTitle = "Made in Abyss",
            synopsis = "Riko y Reg descienden a un abismo lleno de criaturas extrañas y misterios ancestrales.",
            coverImageUrl = "https://cdn.myanimelist.net/images/anime/6/86733.jpg",
            totalEpisodes = 13,
            durationType = DurationType.SHORT,
            emissionStatus = EmissionStatus.ON_AIR,
            releaseYear = 2017,
            genres = listOf(aventura, misterio)
        ),
        Anime(
            id = 4,
            externalApiId = "jujutsu_kaisen",
            title = "Jujutsu Kaisen",
            originalTitle = "Jujutsu Kaisen",
            synopsis = "Itadori se enfrenta a maldiciones para proteger a quienes ama mientras aprende artes ocultas.",
            coverImageUrl = "https://cdn.myanimelist.net/images/anime/1171/109222.jpg",
            totalEpisodes = 24,
            durationType = DurationType.MEDIUM,
            emissionStatus = EmissionStatus.ON_AIR,
            releaseYear = 2020,
            genres = listOf(shonen, misterio)
        ),
        Anime(
            id = 5,
            externalApiId = "monster",
            title = "Monster",
            originalTitle = "Monster",
            synopsis = "El doctor Tenma persigue a un asesino en serie en un thriller psicológico lleno de suspense.",
            coverImageUrl = "https://cdn.myanimelist.net/images/anime/10/18793.jpg",
            totalEpisodes = 74,
            durationType = DurationType.LONG,
            emissionStatus = EmissionStatus.FINISHED,
            releaseYear = 2004,
            genres = listOf(seinen, misterio, drama)
        ),
        Anime(
            id = 6,
            externalApiId = "fullmetal_alchemist_brotherhood",
            title = "Fullmetal Alchemist: Brotherhood",
            originalTitle = "Hagane no Renkinjutsushi",
            synopsis = "Los hermanos Elric buscan la piedra filosofal para recuperar lo que perdieron tras un experimento fallido.",
            coverImageUrl = "https://cdn.myanimelist.net/images/anime/1223/96541.jpg",
            totalEpisodes = 64,
            durationType = DurationType.LONG,
            emissionStatus = EmissionStatus.FINISHED,
            releaseYear = 2009,
            genres = listOf(shonen, aventura, drama)
        )
    )

    private val episodesByAnime: Map<Long, List<Episode>> =
        animeCatalog.associate { anime ->
            anime.id to buildEpisodesFor(anime.title)
        }
    val heroAnime: Anime = animeCatalog.first()

    val preferredGenres: List<Genre> = listOf(shonen, aventura, seinen)

    val defaultUserSettings = UserSettings(
        preferredGenres = preferredGenres,
        preferredDuration = DurationType.MEDIUM,
        notificationsEnabled = true,
        culturalAlertsEnabled = true,
        autoplayNextEpisode = true,
        hasCompletedOnboarding = false
    )

    val defaultUserProfile = UserProfile(
        id = "user-001",
        name = "Akira Morales",
        nickname = "OtakuSensei",
        email = "akira@animedev.io",
        avatarUrl = "https://cdn.myanimelist.net/images/characters/7/284193.jpg",
        knowledgeLevel = "Explorador Cultural",
        xpPoints = 1280,
        biography = "Apasionado por descubrir las referencias históricas y gastronómicas escondidas en cada anime.",
        totalAnimesWatched = 42,
        completedTrivias = 18,
        preferredDuration = defaultUserSettings.preferredDuration,
        favoriteGenres = preferredGenres,
        badges = listOf(
            "Embajador del Shonen",
            "Guardián de los Matsuri",
            "Catador de Onigiris"
        ),
        favoriteQuote = ""
    )

    val defaultTriviaStats = TriviaProfileStats(
        totalAnswered = 120,
        perfectRuns = 7,
        masteryLevel = "Sage del Anime",
        scoresByDifficulty = mapOf(
            TriviaDifficulty.EASY to 95,
            TriviaDifficulty.MEDIUM to 82,
            TriviaDifficulty.HARD to 68
        )
    )

    val recentAnimeHistory: List<Anime> = animeCatalog.take(4)

    fun buildSectionsForGenres(genres: List<Genre>): List<AnimeSection> =
        genres.map { genre ->
            AnimeSection(
                genre = genre,
                animes = animeCatalog.filter { anime ->
                    anime.genres.any { it.id == genre.id }
                }
            )
        }

    fun getAnimeDetail(animeId: Long): AnimeDetail {
        val anime = animeCatalog.firstOrNull { it.id == animeId }
            ?: error("Anime con id $animeId no encontrado")

        return AnimeDetail(
            anime = anime,
            culturalNotes = listOf(
                "Influencias culturales presentes en ${anime.title}",
                "Contexto histórico del año ${anime.releaseYear ?: "N/A"}",
                "Referencias gastronómicas y festividades mostradas en la serie"
            ),
            episodes = episodesByAnime[animeId].orEmpty()
        )
    }

    private fun buildEpisodesFor(title: String): List<Episode> =
        List(8) { index ->
            Episode(
                number = index + 1,
                title = "Episodio ${index + 1}",
                durationMinutes = 24,
                synopsis = "Resumen del episodio ${index + 1} de $title con apuntes culturales relevantes."
            )
        }
}
