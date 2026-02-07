package com.example.animedev20.ui.theme.domain.model.Trivias

enum class TriviaDifficulty(
                            val displayName: String,
                            val description: String
) {
    EASY(
    displayName = "Fácil",
    description = "Preguntas introductorias para calentar motores"
    ),
    MEDIUM(
    displayName = "Media",
    description = "Retos para quienes prestan atención a los detalles"
    ),
    HARD(
    displayName = "Difícil",
    description = "Cuestionarios culturales y de contexto más profundos"
    )
}