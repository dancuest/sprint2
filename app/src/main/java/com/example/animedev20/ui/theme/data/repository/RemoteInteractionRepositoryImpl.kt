package com.example.animedev20.ui.theme.data.repository

import com.example.animedev20.ui.theme.data.remote.InteractionRequest
import com.example.animedev20.ui.theme.data.remote.InteractionsApi
import com.example.animedev20.ui.theme.domain.repository.InteractionRepository

class RemoteInteractionRepositoryImpl(
    private val interactionsApi: InteractionsApi
) : InteractionRepository {

    override suspend fun trackView(animeId: Long) {
        interactionsApi.postInteraction(
            InteractionRequest(
                type = "VIEW",
                animeId = animeId
            )
        )
    }

    override suspend fun trackFavorite(animeId: Long) {
        interactionsApi.postInteraction(
            InteractionRequest(
                type = "FAVORITE",
                animeId = animeId
            )
        )
    }

    override suspend fun trackUnfavorite(animeId: Long) {
        interactionsApi.postInteraction(
            InteractionRequest(
                type = "UNFAVORITE",
                animeId = animeId
            )
        )
    }

    override suspend fun trackTriviaScore(animeId: Long, score: Int, totalQuestions: Int) {
        interactionsApi.postInteraction(
            InteractionRequest(
                type = "TRIVIA_SCORE",
                animeId = animeId,
                payload = mapOf(
                    "score" to score,
                    "totalQuestions" to totalQuestions
                )
            )
        )
    }
}