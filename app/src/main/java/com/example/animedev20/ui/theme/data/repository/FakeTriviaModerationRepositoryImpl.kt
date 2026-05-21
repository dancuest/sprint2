package com.example.animedev20.ui.theme.data.repository

import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaCategory
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaDifficulty
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaModerationQuestion
import com.example.animedev20.ui.theme.domain.repository.TriviaModerationRepository
import kotlinx.coroutines.delay

object FakeTriviaModerationRepositoryImpl : TriviaModerationRepository {

    private val pendingQuestions = mutableListOf(
        TriviaModerationQuestion(
            id = "fake-1",
            animeId = 21L,
            externalAnimeId = "21",
            question = "¿Quién es el protagonista principal de la historia?",
            options = listOf(
                "Monkey D. Luffy",
                "Roronoa Zoro",
                "Sanji",
                "Trafalgar Law"
            ),
            correctAnswerIndex = 0,
            explanation = "Monkey D. Luffy es el protagonista central de One Piece.",
            difficulty = TriviaDifficulty.EASY,
            category = TriviaCategory.CHARACTER,
            status = "PENDING",
            source = "USER_SUBMITTED",
            createdByUserId = "fake-user",
            createdAt = null
        )
    )

    override suspend fun getPendingQuestions(): Result<List<TriviaModerationQuestion>> {
        delay(400)
        return Result.success(pendingQuestions.toList())
    }

    override suspend fun approveQuestion(questionId: String): Result<String> {
        delay(300)
        pendingQuestions.removeAll { question -> question.id == questionId }
        return Result.success("Pregunta aprobada correctamente.")
    }

    override suspend fun rejectQuestion(questionId: String): Result<String> {
        delay(300)
        pendingQuestions.removeAll { question -> question.id == questionId }
        return Result.success("Pregunta rechazada correctamente.")
    }
}