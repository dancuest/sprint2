package com.example.animedev20.ui.theme.domain.model

data class Trailer(
                   val number: Int,
                   val title: String,
                   val durationMinutes: Int,
                   val description: String,
                   val youtubeUrl: String
)

data class AnimeDetail(
                       val anime: Anime,
                       val culturalNotes: List<String> = emptyList(),
                       val trailers: List<Trailer> = emptyList()
)
