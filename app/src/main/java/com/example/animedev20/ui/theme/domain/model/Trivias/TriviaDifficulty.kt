package com.example.animedev20.ui.theme.domain.model.Trivias

enum class TriviaDifficulty(
    val displayName: String,
    val description: String
) {
    EASY(
        displayName = "Fácil",
        description = "3 preguntas introductorias para calentar motores"
    ),
    MEDIUM(
        displayName = "Media",
        description = "5 preguntas para quienes prestan atención a los detalles"
    ),
    HARD(
        displayName = "Difícil",
        description = "8 preguntas culturales y de contexto más profundas"
    )
}