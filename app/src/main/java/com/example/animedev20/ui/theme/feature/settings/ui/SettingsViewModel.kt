package com.example.animedev20.ui.theme.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.animedev20.ui.theme.domain.model.DurationType
import com.example.animedev20.ui.theme.domain.model.Genre
import com.example.animedev20.ui.theme.domain.repository.AnimeRepository
import com.example.animedev20.ui.theme.domain.repository.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userRepository: UserRepository,
    private val animeRepository: AnimeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching {
                val settingsDeferred = async { userRepository.getUserSettings() }
                val profileDeferred = async { userRepository.getUserProfile() }
                val genresDeferred = async { animeRepository.getGenres() }
                Triple(settingsDeferred.await(), profileDeferred.await(), genresDeferred.await())
            }.onSuccess { (settings, profile, genres) ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        availableGenres = genres,
                        selectedGenres = settings.preferredGenres.map { genre -> genre.id }.toSet(),
                        preferredDurations = settings.preferredDurations.toSet(),
                        notificationsEnabled = settings.notificationsEnabled,
                        culturalAlertsEnabled = settings.culturalAlertsEnabled,
                        autoplayNextEpisode = settings.autoplayNextEpisode,
                        hasCompletedOnboarding = settings.hasCompletedOnboarding,
                        name = profile.name,
                        email = profile.email,
                        nickname = profile.nickname,
                        message = null
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = error.message ?: "No pudimos cargar tus ajustes"
                    )
                }
            }
        }
    }

    fun onGenreSelected(genreId: String) {
        _uiState.update { state ->
            val newSelection = state.selectedGenres.toMutableSet().apply {
                if (!add(genreId)) remove(genreId)
            }
            state.copy(selectedGenres = newSelection)
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

    fun onNotificationsToggled(enabled: Boolean) {
        _uiState.update { it.copy(notificationsEnabled = enabled) }
    }

    fun onCulturalAlertsToggled(enabled: Boolean) {
        _uiState.update { it.copy(culturalAlertsEnabled = enabled) }
    }

    fun onAutoplayToggled(enabled: Boolean) {
        _uiState.update { it.copy(autoplayNextEpisode = enabled) }
    }

    fun onNameChanged(value: String) {
        _uiState.update { it.copy(name = value) }
    }

    fun onEmailChanged(value: String) {
        _uiState.update { it.copy(email = value) }
    }

    fun onNicknameChanged(value: String) {
        _uiState.update { it.copy(nickname = value) }
    }

    fun savePreferences() {
        viewModelScope.launch {
            val state = _uiState.value
            val selectedGenres = state.availableGenres.filter { genre ->
                state.selectedGenres.contains(genre.id)
            }
            runCatching { userRepository.updatePreferredGenres(selectedGenres) }
                .onSuccess {
                    _uiState.update { current ->
                        current.copy(message = "Géneros actualizados")
                    }
                }
                .onFailure { error ->
                    _uiState.update { current ->
                        current.copy(message = error.message ?: "No pudimos guardar tus cambios")
                    }
                }
        }
    }

    fun saveAccountInfo() {
        viewModelScope.launch {
            val state = _uiState.value
            runCatching {
                userRepository.updateAccountInfo(state.name, state.email, state.nickname)
            }.onSuccess { updatedProfile ->
                _uiState.update { current ->
                    current.copy(
                        message = "Perfil actualizado correctamente",
                        name = updatedProfile.name,
                        email = updatedProfile.email,
                        nickname = updatedProfile.nickname
                    )
                }
            }.onFailure { error ->
                _uiState.update { current ->
                    current.copy(message = error.message ?: "Hubo un error al guardar tus datos")
                }
            }
        }
    }

    fun onMessageConsumed() {
        _uiState.update { it.copy(message = null) }
    }

    companion object {
        fun provideFactory(
            userRepository: UserRepository,
            animeRepository: AnimeRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                    return SettingsViewModel(userRepository, animeRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}

data class SettingsUiState(
    val isLoading: Boolean = true,
    val availableGenres: List<Genre> = emptyList(),
    val selectedGenres: Set<String> = emptySet(),
    val preferredDurations: Set<DurationType> = setOf(DurationType.MEDIUM),
    val notificationsEnabled: Boolean = true,
    val culturalAlertsEnabled: Boolean = true,
    val autoplayNextEpisode: Boolean = true,
    val hasCompletedOnboarding: Boolean = false,
    val name: String = "",
    val email: String = "",
    val nickname: String = "",
    val message: String? = null
)
