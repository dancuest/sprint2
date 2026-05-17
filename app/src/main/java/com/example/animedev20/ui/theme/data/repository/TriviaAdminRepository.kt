package com.example.animedev20.ui.theme.data.repository

import com.example.animedev20.ui.theme.data.remote.AdminAnimeSearchResponse
import com.example.animedev20.ui.theme.data.remote.AdminTriviaQuestionResponse
import com.example.animedev20.ui.theme.data.remote.CreateQuestionReportRequest
import com.example.animedev20.ui.theme.data.remote.DeleteQuestionResponse
import com.example.animedev20.ui.theme.data.remote.TriviaAdminApi
import com.example.animedev20.ui.theme.data.remote.TriviaQuestionReportResponse
import com.example.animedev20.ui.theme.data.remote.UpdateQuestionReportRequest
import com.example.animedev20.ui.theme.data.remote.UpsertTriviaQuestionRequest

class TriviaAdminRepository(
    private val api: TriviaAdminApi?
) {
    private fun requireApi(): TriviaAdminApi {
        return api ?: error("TriviaAdminApi no está disponible. Revisa ApiConfig.baseUrl, AuthTokenStore o USE_REMOTE.")
    }

    suspend fun reportQuestionError(
        questionId: String,
        animeId: Long?,
        animeTitle: String?,
        questionText: String,
        reason: String
    ): TriviaQuestionReportResponse {
        return requireApi().createQuestionReport(
            CreateQuestionReportRequest(
                questionId = questionId,
                animeId = animeId,
                animeTitle = animeTitle,
                questionText = questionText,
                reason = reason
            )
        )
    }

    suspend fun getQuestionRequests(): List<AdminTriviaQuestionResponse> {
        return requireApi().getAdminQuestions(status = "PENDING")
    }

    suspend fun getQuestionById(questionId: String): AdminTriviaQuestionResponse {
        return requireApi().getAdminQuestionById(questionId)
    }

    suspend fun approveQuestionRequest(questionId: String): AdminTriviaQuestionResponse {
        return requireApi().updateQuestion(
            id = questionId,
            request = UpsertTriviaQuestionRequest(status = "APPROVED")
        )
    }

    suspend fun rejectQuestionRequest(questionId: String): AdminTriviaQuestionResponse {
        return requireApi().updateQuestion(
            id = questionId,
            request = UpsertTriviaQuestionRequest(status = "REJECTED")
        )
    }

    suspend fun getQuestionReports(
        status: String = "PENDING"
    ): List<TriviaQuestionReportResponse> {
        return requireApi().getQuestionReports(status)
    }

    suspend fun updateQuestionReport(
        reportId: String,
        status: String,
        adminNote: String? = null
    ): TriviaQuestionReportResponse {
        return requireApi().updateQuestionReport(
            id = reportId,
            request = UpdateQuestionReportRequest(
                status = status,
                adminNote = adminNote
            )
        )
    }

    suspend fun searchAnime(
        query: String
    ): List<AdminAnimeSearchResponse> {
        if (query.trim().length < 2) return emptyList()
        return requireApi().searchAnimeForTriviaAdmin(query.trim())
    }

    suspend fun getQuestionsByAnime(
        animeId: Long
    ): List<AdminTriviaQuestionResponse> {
        return requireApi().getQuestionsByAnime(animeId)
    }

    suspend fun createQuestionAsAdmin(
        request: UpsertTriviaQuestionRequest
    ): AdminTriviaQuestionResponse {
        return requireApi().createQuestionAsAdmin(request)
    }

    suspend fun updateQuestion(
        questionId: String,
        request: UpsertTriviaQuestionRequest
    ): AdminTriviaQuestionResponse {
        return requireApi().updateQuestion(questionId, request)
    }

    suspend fun deleteQuestion(
        questionId: String
    ): DeleteQuestionResponse {
        return requireApi().deleteQuestion(questionId)
    }
}