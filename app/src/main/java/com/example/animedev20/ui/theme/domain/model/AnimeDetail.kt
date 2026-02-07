package com.example.animedev20.ui.theme.domain.model

data class Episode(
                   val number: Int,
                   val title: String,
                   val durationMinutes: Int,
                   val synopsis: String
)

data class AnimeDetail(
                       val anime: Anime,
                       val culturalNotes: List<String> = emptyList(),
                       val episodes: List<Episode> = emptyList()
)