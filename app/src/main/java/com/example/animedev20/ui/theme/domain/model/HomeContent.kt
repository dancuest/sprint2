package com.example.animedev20.ui.theme.domain.model

data class HomeContent(
    val heroAnime: Anime,
    val preferredGenres: List<Genre>,
    val sections: List<AnimeSection>
)

data class AnimeSection(
    val genre: Genre,
    val animes: List<Anime>,
    val source: String = "genre"
)
