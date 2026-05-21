package com.example.animedev20.ui.theme.domain.repository

import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaModerationQuestion

interface TriviaModerationRepository {

    suspend fun getPendingQuestions(): Result<List<TriviaModerationQuestion>>

    suspend fun approveQuestion(questionId: String): Result<String>

    suspend fun rejectQuestion(questionId: String): Result<String>
}