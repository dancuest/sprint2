package com.example.animedev20.ui.theme.feature.trivia.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.animedev20.ui.theme.domain.model.Anime
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaDifficulty
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaQuestion
import com.example.animedev20.ui.theme.domain.repository.AnimeRepository
import com.example.animedev20.ui.theme.domain.repository.TriviaRepository
import com.example.animedev20.ui.theme.domain.repository.UserRepository
import com.example.animedev20.ui.theme.domain.usecase.GetAnimeDetailUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface TriviaPlayUiState {
    data object Loading : TriviaPlayUiState
    data class Error(val message: String) : TriviaPlayUiState
    data class Success(val state: TriviaPlayState) : TriviaPlayUiState
}

data class TriviaPlayState(
    val anime: Anime,
    val difficulty: TriviaDifficulty? = null,
    val questions: List<TriviaQuestion> = emptyList(),
    val currentIndex: Int = 0,
    val selectedAnswer: Int? = null,
    val isAnswerCorrect: Boolean? = null,
    val score: Int = 0,
    val finished: Boolean = false
) {
    val currentQuestion: TriviaQuestion? = questions.getOrNull(currentIndex)
    val totalQuestions: Int = questions.size
}

class TriviaPlayViewModel(
    private val animeId: Long,
    private val getAnimeDetailUseCase: GetAnimeDetailUseCase,
    private val triviaRepository: TriviaRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TriviaPlayUiState>(TriviaPlayUiState.Loading)
    val uiState: StateFlow<TriviaPlayUiState> = _uiState.asStateFlow()

    init {
        loadAnime()
    }

    private fun loadAnime() {
        viewModelScope.launch {
            val result = getAnimeDetailUseCase(animeId)

            result.fold(
                onSuccess = { detail ->
                    _uiState.value = TriviaPlayUiState.Success(
                        TriviaPlayState(anime = detail.anime)
                    )
                },
                onFailure = { throwable ->
                    _uiState.value = TriviaPlayUiState.Error(
                        throwable.message ?: "No encontramos información del anime."
                    )
                }
            )
        }
    }

    fun selectDifficulty(difficulty: TriviaDifficulty) {
        val current = (_uiState.value as? TriviaPlayUiState.Success)?.state ?: return

        _uiState.value = TriviaPlayUiState.Loading

        viewModelScope.launch {
            runCatching {
                validateTriviaAccessForCurrentUser()
                triviaRepository.getQuestions(animeId, difficulty)
            }.onSuccess { questions ->
                _uiState.value = TriviaPlayUiState.Success(
                    current.copy(
                        difficulty = difficulty,
                        questions = questions,
                        currentIndex = 0,
                        selectedAnswer = null,
                        isAnswerCorrect = null,
                        score = 0,
                        finished = false
                    )
                )
            }.onFailure { throwable ->
                val rawMessage = throwable.message.orEmpty()

                val friendlyMessage = when {
                    rawMessage.contains(FAVORITE_REQUIRED_ERROR, ignoreCase = true) -> {
                        "Para jugar esta trivia, primero agrega este anime a favoritos. " +
                                "Los invitados pueden probar trivias libremente, pero los usuarios registrados deben desbloquearlas desde favoritos."
                    }

                    rawMessage.contains("Anime not found in favorites", ignoreCase = true) -> {
                        "Para jugar esta trivia, primero agrega este anime a favoritos."
                    }

                    else -> {
                        rawMessage.ifBlank { "No pudimos preparar la trivia." }
                    }
                }

                _uiState.value = TriviaPlayUiState.Error(friendlyMessage)
            }
        }
    }

    private suspend fun validateTriviaAccessForCurrentUser() {
        val isGuest = runCatching {
            userRepository.getUserProfile().email.isBlank()
        }.getOrDefault(true)

        if (isGuest) {
            return
        }

        val isAnimeInFavorites = runCatching {
            triviaRepository
                .getTriviaSummaries()
                .first()
                .any { summary -> summary.anime.id == animeId }
        }.getOrDefault(false)

        if (!isAnimeInFavorites) {
            throw IllegalStateException(FAVORITE_REQUIRED_ERROR)
        }
    }

    fun answerQuestion(answerIndex: Int) {
        val state = (_uiState.value as? TriviaPlayUiState.Success)?.state ?: return
        val currentQuestion = state.currentQuestion ?: return

        if (state.selectedAnswer != null) {
            return
        }

        val isCorrect = currentQuestion.correctAnswerIndex == answerIndex
        val newScore = if (isCorrect) state.score + 1 else state.score

        _uiState.value = TriviaPlayUiState.Success(
            state.copy(
                selectedAnswer = answerIndex,
                isAnswerCorrect = isCorrect,
                score = newScore
            )
        )
    }

    fun goToNextQuestion() {
        val content = (_uiState.value as? TriviaPlayUiState.Success)?.state ?: return

        if (content.selectedAnswer == null) {
            return
        }

        val isLastQuestion = content.currentIndex >= content.totalQuestions - 1

        if (isLastQuestion) {
            finishQuiz(content)
        } else {
            _uiState.value = TriviaPlayUiState.Success(
                content.copy(
                    currentIndex = content.currentIndex + 1,
                    selectedAnswer = null,
                    isAnswerCorrect = null
                )
            )
        }
    }

    private fun finishQuiz(state: TriviaPlayState) {
        _uiState.value = TriviaPlayUiState.Success(
            state.copy(finished = true)
        )

        val difficulty = state.difficulty ?: return

        viewModelScope.launch {
            val shouldRecordResult = runCatching {
                userRepository.getUserProfile().email.isNotBlank()
            }.getOrDefault(false)

            if (!shouldRecordResult) {
                return@launch
            }

            triviaRepository.recordResult(
                animeId = animeId,
                difficulty = difficulty,
                score = state.score,
                totalQuestions = state.totalQuestions
            )
        }
    }

    fun restart() {
        val current = (_uiState.value as? TriviaPlayUiState.Success)?.state ?: return

        _uiState.value = TriviaPlayUiState.Success(
            current.copy(
                difficulty = null,
                questions = emptyList(),
                currentIndex = 0,
                selectedAnswer = null,
                isAnswerCorrect = null,
                score = 0,
                finished = false
            )
        )
    }

    fun tryAgain() {
        _uiState.value = TriviaPlayUiState.Loading
        loadAnime()
    }

    companion object {
        private const val FAVORITE_REQUIRED_ERROR = "TRIVIA_REQUIRES_FAVORITE"

        fun provideFactory(
            animeId: Long,
            animeRepository: AnimeRepository,
            triviaRepository: TriviaRepository,
            userRepository: UserRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(TriviaPlayViewModel::class.java)) {
                    val useCase = GetAnimeDetailUseCase(animeRepository)

                    return TriviaPlayViewModel(
                        animeId = animeId,
                        getAnimeDetailUseCase = useCase,
                        triviaRepository = triviaRepository,
                        userRepository = userRepository
                    ) as T
                }

                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}