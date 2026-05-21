package com.example.animedev20.ui.theme.data.remote

import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaDifficulty
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaQuestion

data class RemoteTriviaQuestionDto(
    val id: String,
    val animeId: Long,
    val externalAnimeId: String? = null,
    val question: String,
    val options: List<String> = emptyList(),
    val correctAnswerIndex: Int,
    val explanation: String? = null,
    val difficulty: String,
    val category: String? = null,
    val status: String? = null,
    val source: String? = null,
    val createdByUserId: String? = null,
    val reviewedByUserId: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val reviewedAt: String? = null
)

data class CreateTriviaQuestionRequest(
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val difficulty: String,
    val category: String = "GENERAL",
    val explanation: String? = null,
    val externalAnimeId: String? = null
)

data class RemoteTriviaMutationResponseDto(
    val success: Boolean,
    val message: String,
    val data: RemoteTriviaQuestionDto
)

fun RemoteTriviaQuestionDto.toDomainQuestion(): TriviaQuestion? {
    val cleanQuestion = question.trim()

    if (cleanQuestion.isBlank()) {
        return null
    }

    val cleanOptions = options
        .map { it.trim() }
        .filter { it.isNotBlank() }

    if (cleanOptions.size < 2) {
        return null
    }

    if (correctAnswerIndex !in cleanOptions.indices) {
        return null
    }

    val parsedDifficulty = runCatching {
        TriviaDifficulty.valueOf(difficulty.uppercase())
    }.getOrDefault(TriviaDifficulty.EASY)

    return TriviaQuestion(
        id = id,
        animeId = animeId,
        difficulty = parsedDifficulty,
        question = cleanQuestion,
        options = cleanOptions,
        correctAnswerIndex = correctAnswerIndex,
        feedback = explanation
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: "Respuesta registrada en el banco de preguntas de AnimeDev."
    )
}