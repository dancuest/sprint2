package com.example.animedev20.ui.theme.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

data class InteractionRequest(
    val interactionType: String,
    val animeId: Long? = null,
    val payload: Map<String, Any>? = null
)

interface InteractionsApi {
    @POST("interactions")
    suspend fun postInteraction(@Body request: InteractionRequest)
}
