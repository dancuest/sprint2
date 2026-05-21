package com.example.animedev20.ui.theme.data.repository

import com.example.animedev20.ui.theme.domain.model.Trivias.SubmitTriviaQuestion
import com.example.animedev20.ui.theme.domain.repository.TriviaContributionRepository
import kotlinx.coroutines.delay

object FakeTriviaContributionRepositoryImpl : TriviaContributionRepository {

    override suspend fun submitQuestion(
        animeId: Long,
        question: SubmitTriviaQuestion
    ): Result<String> {
        delay(400)

        val cleanQuestion = question.question.trim()
        val cleanOptions = question.options.map { it.trim() }

        return runCatching {
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

            "Pregunta enviada para revisión."
        }
    }
}