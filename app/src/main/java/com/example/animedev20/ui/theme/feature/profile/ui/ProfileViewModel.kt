package com.example.animedev20.ui.theme.feature.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.animedev20.ui.theme.data.refresh.HomeRefreshBus
import com.example.animedev20.ui.theme.domain.repository.FavoritesRepository
import com.example.animedev20.ui.theme.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val favoritesRepository: FavoritesRepository,
    private val homeRefreshBus: HomeRefreshBus
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        observeProfileUpdates()
        observeRefreshSignals()
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                userRepository.getUserProfile()
                userRepository.getUserSettings()
                runCatching { favoritesRepository.refreshFavorites() }
            } catch (error: Throwable) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "No fue posible cargar el perfil"
                    )
                }
            }
        }
    }

    private fun observeRefreshSignals() {
        viewModelScope.launch {
            homeRefreshBus.events.collect {
                refresh()
            }
        }
    }

    fun updateAvatarImage(imageDataUrl: String) {
        viewModelScope.launch {
            try {
                userRepository.updateProfileImages(avatarUrl = imageDataUrl)
            } catch (error: Throwable) {
                _uiState.update {
                    it.copy(errorMessage = error.message ?: "No fue posible actualizar la foto")
                }
            }
        }
    }

    fun updateCoverImage(imageDataUrl: String) {
        viewModelScope.launch {
            try {
                userRepository.updateProfileImages(coverImageUrl = imageDataUrl)
            } catch (error: Throwable) {
                _uiState.update {
                    it.copy(errorMessage = error.message ?: "No fue posible actualizar la portada")
                }
            }
        }
    }

    private fun observeProfileUpdates() {
        viewModelScope.launch {
            combine(
                userRepository.observeUserProfile(),
                favoritesRepository.favorites
            ) { profile, favoriteAnimes ->
                if (
                    profile.id.isBlank() &&
                    profile.name.isBlank() &&
                    profile.email.isBlank() &&
                    favoriteAnimes.isEmpty()
                ) {
                    return@combine null
                }

                val triviaResolvedCount = profile.completedTrivias
                val favoriteCount = profile.totalAnimesWatched

                ProfileUiState(
                    isLoading = false,
                    profile = profile,
                    favoriteAnimes = favoriteAnimes,
                    totalFavorites = favoriteCount,
                    fanLevel = buildFanLevel(
                        favoriteCount = favoriteCount,
                        triviaCount = triviaResolvedCount
                    ),
                    triviaPlayedCount = triviaResolvedCount,
                    errorMessage = null
                )
            }.collect { state ->
                state?.let { _uiState.value = it }
            }
        }
    }

    private fun buildFanLevel(favoriteCount: Int, triviaCount: Int): String {
        return when {
            favoriteCount >= 20 && triviaCount >= 12 -> "Top Global"
            favoriteCount >= 15 && triviaCount >= 10 -> "OtakuPro"
            favoriteCount >= 7 && triviaCount >= 7 -> "Aprendiz"
            else -> "Novato"
        }
    }

    companion object {
        fun provideFactory(
            userRepository: UserRepository,
            favoritesRepository: FavoritesRepository,
            homeRefreshBus: HomeRefreshBus
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                    return ProfileViewModel(
                        userRepository = userRepository,
                        favoritesRepository = favoritesRepository,
                        homeRefreshBus = homeRefreshBus
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}