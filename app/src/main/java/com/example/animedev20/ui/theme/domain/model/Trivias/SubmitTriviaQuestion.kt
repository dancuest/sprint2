package com.example.animedev20.ui.theme.domain.model.Trivias

data class SubmitTriviaQuestion(
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val difficulty: TriviaDifficulty,
    val category: TriviaCategory,
    val explanation: String? = null,
    val externalAnimeId: String? = null
)