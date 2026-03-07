package com.example.animedev20.ui.theme.data.repository

import android.util.Log
import com.example.animedev20.ui.theme.data.remote.InteractionRequest
import com.example.animedev20.ui.theme.data.remote.InteractionsApi
import com.example.animedev20.ui.theme.domain.repository.InteractionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class RemoteInteractionRepositoryImpl(
    private val interactionsApi: InteractionsApi
) : InteractionRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun trackView(animeId: Long) {
        postAsync(
            InteractionRequest(
                interactionType = "VIEW",
                animeId = animeId
            )
        )
    }

    override fun trackFavorite(animeId: Long) {
        postAsync(
            InteractionRequest(
                interactionType = "FAVORITE",
                animeId = animeId
            )
        )
    }

    override fun trackUnfavorite(animeId: Long) {
        postAsync(
            InteractionRequest(
                interactionType = "UNFAVORITE",
                animeId = animeId
            )
        )
    }

    override fun trackTriviaScore(animeId: Long, score: Int, totalQuestions: Int) {
        postAsync(
            InteractionRequest(
                interactionType = "TRIVIA_SCORE",
                animeId = animeId,
                payload = mapOf(
                    "score" to score,
                    "totalQuestions" to totalQuestions
                )
            )
        )
    }

    private fun postAsync(request: InteractionRequest) {
        scope.launch {
            runCatching { interactionsApi.postInteraction(request) }
                .onFailure { error ->
                    Log.w("Interactions", "Unable to post interaction ${request.interactionType}", error)
                }
        }
    }
}
