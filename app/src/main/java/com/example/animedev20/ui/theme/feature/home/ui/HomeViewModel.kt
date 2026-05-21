package com.example.animedev20.ui.theme.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.animedev20.ui.theme.data.refresh.HomeRefreshBus
import com.example.animedev20.ui.theme.domain.repository.AnimeRepository
import com.example.animedev20.ui.theme.domain.repository.UserRepository
import com.example.animedev20.ui.theme.domain.usecase.GetHomeContentUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getHomeContentUseCase: GetHomeContentUseCase,
    private val homeRefreshBus: HomeRefreshBus
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        observeRefreshSignals()
        loadHomeContent()
    }

    fun loadHomeContent() {
        val previousSelectedGenreId = (_uiState.value as? HomeUiState.Success)?.selectedGenreId

        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            if (_uiState.value !is HomeUiState.Success) {
                _uiState.value = HomeUiState.Loading
            }

            val result = getHomeContentUseCase()

            result.fold(
                onSuccess = { homeContent ->
                    _uiState.value = HomeUiState.Success(
                        homeContent = homeContent,
                        selectedGenreId = previousSelectedGenreId
                    )
                },
                onFailure = { throwable ->
                    if (_uiState.value is HomeUiState.Success) {
                        scheduleRetry()
                    } else {
                        _uiState.value = HomeUiState.Error(
                            throwable.message ?: "Ha ocurrido un error inesperado"
                        )
                    }
                }
            )
        }
    }

    fun toggleGenreFilter(genreId: String) {
        val currentState = _uiState.value as? HomeUiState.Success ?: return

        val updatedGenreId = if (currentState.selectedGenreId == genreId) {
            null
        } else {
            genreId
        }

        _uiState.value = currentState.copy(selectedGenreId = updatedGenreId)
    }

    private fun scheduleRetry() {
        val previousState = _uiState.value as? HomeUiState.Success
        val previousSelectedGenreId = previousState?.selectedGenreId

        viewModelScope.launch {
            delay(4_000L)

            val result = getHomeContentUseCase()
            result.onSuccess { homeContent ->
                _uiState.value = HomeUiState.Success(
                    homeContent = homeContent,
                    selectedGenreId = previousSelectedGenreId
                )
            }
            // Si falla de nuevo, se mantiene el contenido anterior visible.
        }
    }

    private fun observeRefreshSignals() {
        viewModelScope.launch {
            homeRefreshBus.events.collect {
                loadHomeContent()
            }
        }
    }

    companion object {
        fun provideFactory(
            animeRepository: AnimeRepository,
            userRepository: UserRepository,
            homeRefreshBus: HomeRefreshBus
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                    val useCase = GetHomeContentUseCase(animeRepository, userRepository)
                    return HomeViewModel(useCase, homeRefreshBus) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}