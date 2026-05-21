package com.example.animedev20.ui.theme.data.repository

import com.example.animedev20.ui.theme.data.remote.CreateTriviaQuestionRequest
import com.example.animedev20.ui.theme.data.remote.TriviaApi
import com.example.animedev20.ui.theme.domain.model.Trivias.SubmitTriviaQuestion
import com.example.animedev20.ui.theme.domain.repository.TriviaContributionRepository

class RemoteTriviaContributionRepositoryImpl(
    private val triviaApi: TriviaApi
) : TriviaContributionRepository {

    override suspend fun submitQuestion(
        animeId: Long,
        question: SubmitTriviaQuestion
    ): Result<String> {
        return runCatching {
            validateQuestion(question)

            val response = triviaApi.submitQuestion(
                animeId = animeId,
                request = CreateTriviaQuestionRequest(
                    question = question.question.trim(),
                    options = question.options.map { it.trim() },
                    correctAnswerIndex = question.correctAnswerIndex,
                    difficulty = question.difficulty.name,
                    category = question.category.name,
                    explanation = question.explanation
                        ?.trim()
                        ?.takeIf { it.isNotBlank() },
                    externalAnimeId = question.externalAnimeId
                )
            )

            if (!response.success) {
                error(response.message.ifBlank { "No se pudo enviar la pregunta." })
            }

            response.message.ifBlank {
                "Pregunta enviada para revisión."
            }
        }
    }

    private fun validateQuestion(question: SubmitTriviaQuestion) {
        val cleanQuestion = question.question.trim()
        val cleanOptions = question.options.map { it.trim() }

        require(cleanQuestion.length >= 10) {
            "La pregunta debe tener mínimo 10 caracteres."
        }

        require(cleanOptions.size == 4) {
            "Debes registrar exactamente 4 opciones."
        }

        require(cleanOptions.all { it.isNotBlank() }) {
            "Todas las opciones deben tener texto."
        }

        require(question.correctAnswerIndex in cleanOptions.indices) {
            "Debes seleccionar una respuesta correcta válida."
        }

        require(cleanOptions.distinctBy { it.lowercase() }.size == cleanOptions.size) {
            "Las opciones no pueden repetirse."
        }
    }
}