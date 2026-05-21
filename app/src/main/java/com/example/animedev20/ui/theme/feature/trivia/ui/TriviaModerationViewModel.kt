package com.example.animedev20.ui.theme.feature.trivia.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaModerationQuestion
import com.example.animedev20.ui.theme.domain.repository.TriviaModerationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TriviaModerationUiState(
    val isLoading: Boolean = true,
    val isProcessing: Boolean = false,
    val processingQuestionId: String? = null,
    val questions: List<TriviaModerationQuestion> = emptyList(),
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class TriviaModerationViewModel(
    private val triviaModerationRepository: TriviaModerationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TriviaModerationUiState())
    val uiState: StateFlow<TriviaModerationUiState> = _uiState.asStateFlow()

    init {
        loadPendingQuestions()
    }

    fun loadPendingQuestions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )

            triviaModerationRepository.getPendingQuestions()
                .fold(
                    onSuccess = { questions ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            questions = questions,
                            errorMessage = null
                        )
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = error.toFriendlyMessage()
                        )
                    }
                )
        }
    }

    fun approveQuestion(questionId: String) {
        processQuestion(
            questionId = questionId,
            action = triviaModerationRepository::approveQuestion
        )
    }

    fun rejectQuestion(questionId: String) {
        processQuestion(
            questionId = questionId,
            action = triviaModerationRepository::rejectQuestion
        )
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            successMessage = null,
            errorMessage = null
        )
    }

    private fun processQuestion(
        questionId: String,
        action: suspend (String) -> Result<String>
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isProcessing = true,
                processingQuestionId = questionId,
                errorMessage = null,
                successMessage = null
            )

            action(questionId).fold(
                onSuccess = { message ->
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        processingQuestionId = null,
                        questions = _uiState.value.questions.filterNot { question ->
                            question.id == questionId
                        },
                        successMessage = message,
                        errorMessage = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        processingQuestionId = null,
                        errorMessage = error.toFriendlyMessage(),
                        successMessage = null
                    )
                }
            )
        }
    }

    private fun Throwable.toFriendlyMessage(): String {
        val rawMessage = message.orEmpty()

        return when {
            rawMessage.contains("403", ignoreCase = true) ||
                    rawMessage.contains("Forbidden", ignoreCase = true) ->
                "No tienes permisos de moderación. Verifica que tu usuario tenga rol ADMIN o MODERATOR."

            rawMessage.contains("401", ignoreCase = true) ||
                    rawMessage.contains("Unauthorized", ignoreCase = true) ->
                "Tu sesión no es válida o expiró. Inicia sesión nuevamente."

            rawMessage.isNotBlank() -> rawMessage

            else -> "Ocurrió un error al procesar la solicitud."
        }
    }

    companion object {
        fun provideFactory(
            triviaModerationRepository: TriviaModerationRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(TriviaModerationViewModel::class.java)) {
                        return TriviaModerationViewModel(
                            triviaModerationRepository = triviaModerationRepository
                        ) as T
                    }

                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}