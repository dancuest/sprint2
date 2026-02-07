package com.example.animedev20.ui.theme.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.animedev20.ui.theme.data.repository.FakeAnimeRepositoryImpl
import com.example.animedev20.ui.theme.data.repository.FakeUserRepositoryImpl
import com.example.animedev20.ui.theme.domain.usecase.GetHomeContentUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getHomeContentUseCase: GetHomeContentUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeContent()
    }

    fun loadHomeContent() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            val result = getHomeContentUseCase()
            result.fold(
                onSuccess = { homeContent ->
                    _uiState.value = HomeUiState.Success(homeContent)
                },
                onFailure = { throwable ->
                    _uiState.value = HomeUiState.Error(
                        throwable.message ?: "Ha ocurrido un error inesperado"
                    )
                }
            )
        }
    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                    val animeRepository = FakeAnimeRepositoryImpl()
                    val userRepository = FakeUserRepositoryImpl
                    val useCase = GetHomeContentUseCase(animeRepository, userRepository)
                    return HomeViewModel(useCase) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
