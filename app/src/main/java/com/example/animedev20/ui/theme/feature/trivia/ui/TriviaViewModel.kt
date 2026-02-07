package com.example.animedev20.ui.theme.feature.trivia.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.animedev20.ui.theme.data.repository.FakeTriviaRepositoryImpl
import com.example.animedev20.ui.theme.domain.model.Trivias.TriviaSummary
import com.example.animedev20.ui.theme.domain.repository.TriviaRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed interface TriviaUiState {
    data object Loading : TriviaUiState
    data class Error(val message: String) : TriviaUiState
    data class Success(val summaries: List<TriviaSummary>) : TriviaUiState
}

class TriviaViewModel(
    private val triviaRepository: TriviaRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TriviaUiState>(TriviaUiState.Loading)
    val uiState: StateFlow<TriviaUiState> = _uiState.asStateFlow()
    private var observeJob: Job? = null

    init {
        observeSummaries()
    }

    private fun observeSummaries() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            triviaRepository.getTriviaSummaries()
                .catch { throwable ->
                    _uiState.value = TriviaUiState.Error(
                        throwable.message ?: "No pudimos cargar las trivias disponibles"
                    )
                }
                .collectLatest { summaries ->
                    _uiState.value = TriviaUiState.Success(summaries)
                }
        }
    }

    fun retry() {
        _uiState.value = TriviaUiState.Loading
        observeSummaries()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(TriviaViewModel::class.java)) {
                    return TriviaViewModel(FakeTriviaRepositoryImpl) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}