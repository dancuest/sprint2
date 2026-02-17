package com.example.animedev20.ui.theme.domain.model.Trivias

enum class TriviaDifficulty(
    val displayName: String,
    val description: String,
    val questionCount: Int
) {
    EASY(
    displayName = "Fácil",
    description = "Preguntas introductorias para calentar motores",
    questionCount = 4
    ),
    MEDIUM(
    displayName = "Media",
    description = "Retos para quienes prestan atención a los detalles",
    questionCount = 7
    ),
    HARD(
    displayName = "Difícil",
    description = "Cuestionarios culturales y de contexto más profundos",
    questionCount = 12
    )
}