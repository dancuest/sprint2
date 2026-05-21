package com.example.animedev20.ui.theme.feature.trivia.ui

import com.example.animedev20.ui.theme.data.remote.AdminAnimeSearchResponse
import com.example.animedev20.ui.theme.data.remote.AdminTriviaQuestionResponse
import com.example.animedev20.ui.theme.data.remote.TriviaQuestionReportResponse

enum class AdminTriviaSection(
    val label: String
) {
    REQUESTS("Solicitudes"),
    REPORTS("Quejas"),
    BANK("Banco por anime")
}

data class AdminQuestionFormState(
    val id: String? = null,
    val animeId: Long? = null,
    val externalAnimeId: String? = null,
    val question: String = "",
    val options: List<String> = listOf("", "", "", ""),
    val correctAnswerIndex: Int = 0,
    val difficulty: String = "EASY",
    val category: String = "GENERAL",
    val explanation: String = "",
    val status: String = "APPROVED",
    val resolveReportId: String? = null
)

data class AdminTriviaUiState(
    val selectedSection: AdminTriviaSection = AdminTriviaSection.REQUESTS,
    val isLoading: Boolean = false,
    val isActionLoading: Boolean = false,
    val message: String? = null,

    val questionRequests: List<AdminTriviaQuestionResponse> = emptyList(),

    val reportStatusFilter: String = "PENDING",
    val questionReports: List<TriviaQuestionReportResponse> = emptyList(),

    val animeQuery: String = "",
    val animeResults: List<AdminAnimeSearchResponse> = emptyList(),
    val selectedAnime: AdminAnimeSearchResponse? = null,
    val animeQuestions: List<AdminTriviaQuestionResponse> = emptyList(),

    val editorVisible: Boolean = false,
    val editorForm: AdminQuestionFormState = AdminQuestionFormState()
)