package com.example.animedev20.ui.theme.feature.trivia.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.animedev20.ui.theme.data.repository.TriviaAdminRepository
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaQuestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TriviaReportUiState(
    val isSending: Boolean = false,
    val message: String? = null,
    val reportSent: Boolean = false
)

class TriviaReportViewModel(
    private val triviaAdminRepository: TriviaAdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TriviaReportUiState())
    val uiState: StateFlow<TriviaReportUiState> = _uiState.asStateFlow()

    fun reportQuestion(
        question: TriviaQuestion,
        animeTitle: String?,
        reason: String
    ) {
        val cleanReason = reason.trim()

        if (cleanReason.length < 8) {
            _uiState.value = _uiState.value.copy(
                message = "Describe un poco mejor el problema de la pregunta."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSending = true,
                message = null,
                reportSent = false
            )

            runCatching {
                triviaAdminRepository.reportQuestionError(
                    questionId = question.id,
                    animeId = question.animeId,
                    animeTitle = animeTitle,
                    questionText = question.question,
                    reason = cleanReason
                )
            }.onSuccess {
                _uiState.value = TriviaReportUiState(
                    isSending = false,
                    message = "Reporte enviado. El admin lo revisará.",
                    reportSent = true
                )
            }.onFailure { error ->
                _uiState.value = TriviaReportUiState(
                    isSending = false,
                    message = error.message ?: "No fue posible enviar el reporte.",
                    reportSent = false
                )
            }
        }
    }

    fun consumeMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    fun consumeReportSent() {
        _uiState.value = _uiState.value.copy(reportSent = false)
    }

    companion object {
        fun provideFactory(
            triviaAdminRepository: TriviaAdminRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TriviaReportViewModel(
                    triviaAdminRepository = triviaAdminRepository
                ) as T
            }
        }
    }
}