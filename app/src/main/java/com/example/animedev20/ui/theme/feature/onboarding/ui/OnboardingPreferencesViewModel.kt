package com.example.animedev20.ui.theme.feature.onboarding.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.animedev20.ui.theme.domain.model.DurationType
import com.example.animedev20.ui.theme.domain.model.Genre
import com.example.animedev20.ui.theme.domain.model.UserSettings
import com.example.animedev20.ui.theme.domain.repository.AnimeRepository
import com.example.animedev20.ui.theme.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OnboardingPreferencesViewModel(
    private val userRepository: UserRepository,
    private val animeRepository: AnimeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingPreferencesUiState())
    val uiState: StateFlow<OnboardingPreferencesUiState> = _uiState.asStateFlow()

    init {
        loadPreferences()
    }

    fun retryLoading() {
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val settings = userRepository.getUserSettings()
                val genres = animeRepository.getGenres()
                settings to genres
            }
                .onSuccess { (settings, genres) ->
                    val shouldPrefill = settings.hasCompletedOnboarding
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            availableGenres = genres,
                            selectedGenres = if (shouldPrefill) {
                                settings.preferredGenres.map(Genre::id).toSet()
                            } else {
                                emptySet()
                            },
                            preferredDurations = if (shouldPrefill) {
                                settings.preferredDurations.toSet()
                            } else {
                                emptySet()
                            },
                            notificationsEnabled = settings.notificationsEnabled,
                            culturalAlertsEnabled = settings.culturalAlertsEnabled,
                            autoplayNextEpisode = if (shouldPrefill) {
                                settings.autoplayNextEpisode
                            } else {
                                true
                            },
                            hasCompletedOnboarding = settings.hasCompletedOnboarding,
                            errorMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message
                                ?: "No pudimos cargar tus preferencias. Intenta de nuevo."
                        )
                    }
                }
        }
    }

    fun onGenreSelected(genreId: String) {
        _uiState.update { state ->
            val updated = state.selectedGenres.toMutableSet().apply {
                if (!add(genreId)) remove(genreId)
            }
            state.copy(selectedGenres = updated)
        }
    }

    fun onDurationSelected(duration: DurationType) {
        _uiState.update { state ->
            val updated = state.preferredDurations.toMutableSet().apply {
                if (!add(duration)) remove(duration)
            }
            state.copy(preferredDurations = updated)
        }
    }

    fun onContinue() {
        val currentState = _uiState.value
        if (currentState.isSaving || currentState.selectedGenres.isEmpty()) return
        if (currentState.preferredDurations.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val selectedGenres = currentState.availableGenres.filter { genre ->
                currentState.selectedGenres.contains(genre.id)
            }
            val preferredDurations = DurationType.values().filter { duration ->
                currentState.preferredDurations.contains(duration)
            }
            val settings = UserSettings(
                preferredGenres = selectedGenres,
                preferredDurations = preferredDurations,
                notificationsEnabled = currentState.notificationsEnabled,
                culturalAlertsEnabled = currentState.culturalAlertsEnabled,
                autoplayNextEpisode = currentState.autoplayNextEpisode,
                hasCompletedOnboarding = true
            )
            runCatching { userRepository.updateUserSettings(settings) }
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, completed = true) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = error.message
                                ?: "No pudimos guardar tus preferencias. Intenta nuevamente."
                        )
                    }
                }
        }
    }

    companion object {
        fun provideFactory(
            userRepository: UserRepository,
            animeRepository: AnimeRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(OnboardingPreferencesViewModel::class.java)) {
                    return OnboardingPreferencesViewModel(userRepository, animeRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}

data class OnboardingPreferencesUiState(
    val isLoading: Boolean = true,
    val availableGenres: List<Genre> = emptyList(),
    val selectedGenres: Set<String> = emptySet(),
    val preferredDurations: Set<DurationType> = emptySet(),
    val notificationsEnabled: Boolean = true,
    val culturalAlertsEnabled: Boolean = true,
    val autoplayNextEpisode: Boolean = true,
    val hasCompletedOnboarding: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val completed: Boolean = false
)
