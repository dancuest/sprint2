package com.example.animedev20.ui.theme.data.repository

import com.example.animedev20.ui.theme.data.remote.RemoteTriviaQuestionDto
import com.example.animedev20.ui.theme.data.remote.TriviaApi
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaCategory
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaDifficulty
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaModerationQuestion
import com.example.animedev20.ui.theme.domain.repository.TriviaModerationRepository

class RemoteTriviaModerationRepositoryImpl(
    private val triviaApi: TriviaApi
) : TriviaModerationRepository {

    override suspend fun getPendingQuestions(): Result<List<TriviaModerationQuestion>> {
        return runCatching {
            triviaApi.getPendingQuestions(limit = 50)
                .data
                .mapNotNull { dto -> dto.toModerationQuestion() }
        }
    }

    override suspend fun approveQuestion(questionId: String): Result<String> {
        return runCatching {
            val response = triviaApi.approveQuestion(questionId)

            if (!response.success) {
                error(response.message.ifBlank { "No se pudo aprobar la pregunta." })
            }

            response.message.ifBlank {
                "Pregunta aprobada correctamente."
            }
        }
    }

    override suspend fun rejectQuestion(questionId: String): Result<String> {
        return runCatching {
            val response = triviaApi.rejectQuestion(questionId)

            if (!response.success) {
                error(response.message.ifBlank { "No se pudo rechazar la pregunta." })
            }

            response.message.ifBlank {
                "Pregunta rechazada correctamente."
            }
        }
    }

    private fun RemoteTriviaQuestionDto.toModerationQuestion(): TriviaModerationQuestion? {
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

        val parsedCategory = runCatching {
            TriviaCategory.valueOf(category.orEmpty().uppercase())
        }.getOrDefault(TriviaCategory.GENERAL)

        return TriviaModerationQuestion(
            id = id,
            animeId = animeId,
            externalAnimeId = externalAnimeId,
            question = cleanQuestion,
            options = cleanOptions,
            correctAnswerIndex = correctAnswerIndex,
            explanation = explanation?.trim()?.takeIf { it.isNotBlank() },
            difficulty = parsedDifficulty,
            category = parsedCategory,
            status = status.orEmpty(),
            source = source.orEmpty(),
            createdByUserId = createdByUserId,
            createdAt = createdAt
        )
    }
}