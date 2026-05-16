package com.example.animedev20.ui.theme.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface TriviaApi {

    @GET("trivia/anime/{animeId}/questions")
    suspend fun getApprovedQuestions(
        @Path("animeId") animeId: Long,
        @Query("difficulty") difficulty: String? = null,
        @Query("limit") limit: Int = 10
    ): ApiResponse<List<RemoteTriviaQuestionDto>>

    @POST("trivia/anime/{animeId}/questions")
    suspend fun submitQuestion(
        @Path("animeId") animeId: Long,
        @Body request: CreateTriviaQuestionRequest
    ): RemoteTriviaMutationResponseDto

    @GET("trivia/me/questions")
    suspend fun getMyQuestions(
        @Query("difficulty") difficulty: String? = null,
        @Query("limit") limit: Int = 20
    ): ApiResponse<List<RemoteTriviaQuestionDto>>

    @GET("trivia/pending")
    suspend fun getPendingQuestions(
        @Query("difficulty") difficulty: String? = null,
        @Query("limit") limit: Int = 20
    ): ApiResponse<List<RemoteTriviaQuestionDto>>

    @PATCH("trivia/questions/{questionId}/approve")
    suspend fun approveQuestion(
        @Path("questionId") questionId: String
    ): RemoteTriviaMutationResponseDto

    @PATCH("trivia/questions/{questionId}/reject")
    suspend fun rejectQuestion(
        @Path("questionId") questionId: String
    ): RemoteTriviaMutationResponseDto
}