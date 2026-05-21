package com.example.animedev20.ui.theme.domain.model.Trivias

data class TriviaModerationQuestion(
    val id: String,
    val animeId: Long,
    val externalAnimeId: String?,
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val explanation: String?,
    val difficulty: TriviaDifficulty,
    val category: TriviaCategory,
    val status: String,
    val source: String,
    val createdByUserId: String?,
    val createdAt: String?
) {
    val correctAnswer: String
        get() = options.getOrNull(correctAnswerIndex).orEmpty()
}