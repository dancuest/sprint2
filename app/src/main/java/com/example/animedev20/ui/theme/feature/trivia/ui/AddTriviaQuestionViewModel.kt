package com.example.animedev20.ui.theme.feature.trivia.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.animedev20.ui.theme.domain.model.Trivias.SubmitTriviaQuestion
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaCategory
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaDifficulty
import com.example.animedev20.ui.theme.domain.repository.TriviaContributionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddTriviaQuestionUiState(
    val question: String = "",
    val optionA: String = "",
    val optionB: String = "",
    val optionC: String = "",
    val optionD: String = "",
    val correctAnswerIndex: Int = 0,
    val difficulty: TriviaDifficulty = TriviaDifficulty.EASY,
    val category: TriviaCategory = TriviaCategory.CHARACTER,
    val explanation: String = "",
    val isSubmitting: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
) {
    val options: List<String>
        get() = listOf(optionA, optionB, optionC, optionD)
}

class AddTriviaQuestionViewModel(
    private val animeId: Long,
    private val triviaContributionRepository: TriviaContributionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTriviaQuestionUiState())
    val uiState: StateFlow<AddTriviaQuestionUiState> = _uiState.asStateFlow()

    fun updateQuestion(value: String) {
        updateState {
            copy(
                question = value,
                successMessage = null,
                errorMessage = null
            )
        }
    }

    fun updateOption(index: Int, value: String) {
        updateState {
            when (index) {
                0 -> copy(optionA = value, successMessage = null, errorMessage = null)
                1 -> copy(optionB = value, successMessage = null, errorMessage = null)
                2 -> copy(optionC = value, successMessage = null, errorMessage = null)
                3 -> copy(optionD = value, successMessage = null, errorMessage = null)
                else -> this
            }
        }
    }

    fun updateCorrectAnswerIndex(index: Int) {
        updateState {
            copy(
                correctAnswerIndex = index,
                successMessage = null,
                errorMessage = null
            )
        }
    }

    fun updateDifficulty(value: TriviaDifficulty) {
        updateState {
            copy(
                difficulty = value,
                successMessage = null,
                errorMessage = null
            )
        }
    }

    fun updateCategory(value: TriviaCategory) {
        updateState {
            copy(
                category = value,
                successMessage = null,
                errorMessage = null
            )
        }
    }

    fun updateExplanation(value: String) {
        updateState {
            copy(
                explanation = value,
                successMessage = null,
                errorMessage = null
            )
        }
    }

    fun submit() {
        val current = uiState.value
        val validationError = validate(current)

        if (validationError != null) {
            updateState {
                copy(
                    errorMessage = validationError,
                    successMessage = null
                )
            }
            return
        }

        viewModelScope.launch {
            updateState {
                copy(
                    isSubmitting = true,
                    errorMessage = null,
                    successMessage = null
                )
            }

            val result = triviaContributionRepository.submitQuestion(
                animeId = animeId,
                question = SubmitTriviaQuestion(
                    question = current.question.trim(),
                    options = current.options.map { it.trim() },
                    correctAnswerIndex = current.correctAnswerIndex,
                    difficulty = current.difficulty,
                    category = current.category,
                    explanation = current.explanation
                        .trim()
                        .takeIf { it.isNotBlank() },
                    externalAnimeId = animeId.toString()
                )
            )

            result.fold(
                onSuccess = { message ->
                    updateState {
                        AddTriviaQuestionUiState(
                            successMessage = message
                        )
                    }
                },
                onFailure = { error ->
                    updateState {
                        copy(
                            isSubmitting = false,
                            errorMessage = error.message ?: "No se pudo enviar la pregunta."
                        )
                    }
                }
            )
        }
    }

    private fun validate(state: AddTriviaQuestionUiState): String? {
        val question = state.question.trim()
        val options = state.options.map { it.trim() }

        if (question.length < 10) {
            return "La pregunta debe tener mínimo 10 caracteres."
        }

        if (!question.endsWith("?") && !question.endsWith("¿")) {
            return "La pregunta debería terminar con signo de interrogación."
        }

        if (options.any { it.isBlank() }) {
            return "Completa las 4 opciones de respuesta."
        }

        if (options.distinctBy { it.lowercase() }.size != options.size) {
            return "Las opciones no pueden repetirse."
        }

        if (state.correctAnswerIndex !in options.indices) {
            return "Selecciona una respuesta correcta."
        }

        return null
    }

    private fun updateState(
        reducer: AddTriviaQuestionUiState.() -> AddTriviaQuestionUiState
    ) {
        _uiState.value = _uiState.value.reducer()
    }

    companion object {
        fun provideFactory(
            animeId: Long,
            triviaContributionRepository: TriviaContributionRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(AddTriviaQuestionViewModel::class.java)) {
                        return AddTriviaQuestionViewModel(
                            animeId = animeId,
                            triviaContributionRepository = triviaContributionRepository
                        ) as T
                    }

                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
    }
}