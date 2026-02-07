package com.example.animedev20.ui.theme.domain.model

enum class EmissionStatus {
    ON_AIR, FINISHED, ON_BREAK
}

enum class DurationType {
    SHORT, MEDIUM, LONG
}

data class Anime(
                 val id: Long,
                 val externalApiId: String,
                 val title: String,
                 val originalTitle: String?,
                 val synopsis: String,
                 val coverImageUrl: String,
                 val mangaPlusUrl: String,
                 val totalEpisodes: Int?,
                 val durationType: DurationType,
                 val emissionStatus: EmissionStatus,
                 val releaseYear: Int?,
                 val genres: List<Genre> = emptyList()
)
