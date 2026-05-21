package com.example.animedev20.ui.theme.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class CreateQuestionReportRequest(
    val questionId: String,
    val animeId: Long?,
    val animeTitle: String?,
    val questionText: String,
    val reason: String
)

data class TriviaQuestionReportResponse(
    val id: String,
    val questionId: String,
    val animeId: Long?,
    val animeTitle: String?,
    val questionText: String,
    val reason: String,
    val status: String,
    val reporterUserId: String?,
    val resolvedByUserId: String?,
    val adminNote: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val resolvedAt: String?
)

data class UpdateQuestionReportRequest(
    val status: String? = null,
    val adminNote: String? = null
)

data class AdminAnimeSearchResponse(
    val id: Long,
    val externalApiId: String?,
    val title: String,
    val originalTitle: String?,
    val coverImageUrl: String?,
    val totalEpisodes: Int?,
    val releaseYear: Int?
)

data class AdminTriviaQuestionResponse(
    val id: String,
    val animeId: Long?,
    val externalAnimeId: String?,
    val animeTitle: String? = null,
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val difficulty: String,
    val category: String?,
    val explanation: String?,
    val status: String?,
    val createdAt: String?,
    val updatedAt: String?
)

data class UpsertTriviaQuestionRequest(
    val animeId: Long? = null,
    val externalAnimeId: String? = null,
    val question: String? = null,
    val options: List<String>? = null,
    val correctAnswerIndex: Int? = null,
    val difficulty: String? = null,
    val category: String? = null,
    val explanation: String? = null,
    val status: String? = null
)

data class DeleteQuestionResponse(
    val success: Boolean,
    val message: String,
    val question: AdminTriviaQuestionResponse?
)

interface TriviaAdminApi {

    @POST("trivia/question-reports")
    suspend fun createQuestionReport(
        @Body request: CreateQuestionReportRequest
    ): TriviaQuestionReportResponse

    @GET("trivia/admin/question-reports")
    suspend fun getQuestionReports(
        @Query("status") status: String = "PENDING"
    ): List<TriviaQuestionReportResponse>

    @PATCH("trivia/admin/question-reports/{id}")
    suspend fun updateQuestionReport(
        @Path("id") id: String,
        @Body request: UpdateQuestionReportRequest
    ): TriviaQuestionReportResponse

    @GET("trivia/admin/questions")
    suspend fun getAdminQuestions(
        @Query("status") status: String = "PENDING"
    ): List<AdminTriviaQuestionResponse>

    @GET("trivia/admin/questions/{id}")
    suspend fun getAdminQuestionById(
        @Path("id") id: String
    ): AdminTriviaQuestionResponse

    @GET("trivia/admin/anime-search")
    suspend fun searchAnimeForTriviaAdmin(
        @Query("q") query: String
    ): List<AdminAnimeSearchResponse>

    @GET("trivia/admin/anime/{animeId}/questions")
    suspend fun getQuestionsByAnime(
        @Path("animeId") animeId: Long
    ): List<AdminTriviaQuestionResponse>

    @POST("trivia/admin/questions")
    suspend fun createQuestionAsAdmin(
        @Body request: UpsertTriviaQuestionRequest
    ): AdminTriviaQuestionResponse

    @PATCH("trivia/admin/questions/{id}")
    suspend fun updateQuestion(
        @Path("id") id: String,
        @Body request: UpsertTriviaQuestionRequest
    ): AdminTriviaQuestionResponse

    @DELETE("trivia/admin/questions/{id}")
    suspend fun deleteQuestion(
        @Path("id") id: String
    ): DeleteQuestionResponse
}