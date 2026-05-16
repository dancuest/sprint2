package com.example.animedev20.ui.theme.domain.repository

import com.example.animedev20.ui.theme.domain.model.Trivias.SubmitTriviaQuestion

interface TriviaContributionRepository {

    suspend fun submitQuestion(
        animeId: Long,
        question: SubmitTriviaQuestion
    ): Result<String>
}