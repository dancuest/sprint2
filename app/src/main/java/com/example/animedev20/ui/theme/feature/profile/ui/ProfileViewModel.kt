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
                val favoriteCount = favoriteAnimes.size

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

    /**
     * Escala de nivel fan basada en la combinación de favoritos y trivias resueltas:
     *
     * a) Novato:
     *    - no más de 10 animes en favoritos
     *    - menos de 6 trivias resueltas
     *
     * b) Aprendiz:
     *    - entre 11 y 20 favoritos
     *    - menos de 11 trivias resueltas
     *
     * c) OtakuPro:
     *    - entre 20 y 26 favoritos
     *    - entre 11 y 15 trivias resueltas
     *
     * d) Top Global:
     *    - más de 26 favoritos
     *    - más de 15 trivias resueltas
     *
     * Para evitar zonas ambiguas entre rangos, se priorizan primero los niveles
     * superiores y luego se aplica una degradación razonable cuando el usuario
     * supera parcialmente un umbral.
     */
    private fun buildFanLevel(favoriteCount: Int, triviaCount: Int): String {
        return when {
            favoriteCount > 26 && triviaCount > 15 -> "Top Global"

            favoriteCount in 20..26 && triviaCount in 11..15 -> "OtakuPro"

            favoriteCount in 11..20 && triviaCount < 11 -> "Aprendiz"

            favoriteCount <= 10 && triviaCount < 6 -> "Novato"

            // Casos intermedios o mixtos: se asigna el nivel más cercano superior
            favoriteCount > 26 || triviaCount > 15 -> "Top Global"
            favoriteCount >= 20 || triviaCount >= 11 -> "OtakuPro"
            favoriteCount >= 11 || triviaCount >= 6 -> "Aprendiz"
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