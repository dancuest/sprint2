package com.example.animedev20.ui.theme.feature.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        observeProfileUpdates()
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                userRepository.getUserProfile()
                userRepository.getUserSettings()
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

                val triviaPlayedCount = profile.completedTrivias
                val favoriteCount = favoriteAnimes.size

                ProfileUiState(
                    isLoading = false,
                    profile = profile.copy(totalAnimesWatched = favoriteCount),
                    favoriteAnimes = favoriteAnimes,
                    fanLevel = buildFanLevel(
                        favoriteCount = favoriteCount,
                        triviaCount = triviaPlayedCount
                    ),
                    triviaPlayedCount = triviaPlayedCount,
                    errorMessage = null
                )
            }.collect { state ->
                state?.let { _uiState.value = it }
            }
        }
    }

    private fun buildFanLevel(favoriteCount: Int, triviaCount: Int): String {
        val score = favoriteCount + triviaCount

        return when {
            score >= 20 -> "Otaku maestro"
            score >= 12 -> "Muy fan del anime"
            score >= 6 -> "Fan en crecimiento"
            else -> "Explorador del anime"
        }
    }

    companion object {
        fun provideFactory(
            userRepository: UserRepository,
            favoritesRepository: FavoritesRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
                    return ProfileViewModel(
                        userRepository = userRepository,
                        favoritesRepository = favoritesRepository
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}